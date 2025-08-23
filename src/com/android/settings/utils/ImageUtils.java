/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static String saveImageToInternalStorage(Context context, Uri imgUri, String featurePath, String filePrefix) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        try {
            // Abrir el InputStream
            if (imgUri.toString().startsWith("content://com.google.android.apps.photos.contentprovider")) {
                List<String> segments = imgUri.getPathSegments();
                if (segments.size() > 2) {
                    String mediaUriString = URLDecoder.decode(segments.get(2), StandardCharsets.UTF_8.name());
                    Uri mediaUri = Uri.parse(mediaUriString);
                    inputStream = context.getContentResolver().openInputStream(mediaUri);
                } else {
                    throw new FileNotFoundException("Failed to parse Google Photos content URI");
                }
            } else {
                inputStream = context.getContentResolver().openInputStream(imgUri);
            }

            if (inputStream == null) {
                Log.e(TAG, "InputStream is null");
                return null;
            }

            // Decodificar el bitmap con opciones para evitar OOM
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            
            // Calcular sample size para reducir la memoria
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
            options.inJustDecodeBounds = false;
            
            // Cerrar y reabrir el stream
            inputStream.close();
            if (imgUri.toString().startsWith("content://com.google.android.apps.photos.contentprovider")) {
                List<String> segments = imgUri.getPathSegments();
                if (segments.size() > 2) {
                    String mediaUriString = URLDecoder.decode(segments.get(2), StandardCharsets.UTF_8.name());
                    Uri mediaUri = Uri.parse(mediaUriString);
                    inputStream = context.getContentResolver().openInputStream(mediaUri);
                }
            } else {
                inputStream = context.getContentResolver().openInputStream(imgUri);
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return null;
            }

            // Usar el directorio de archivos internos de la app
            File directory = new File(context.getFilesDir(), featurePath);
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                return null;
            }

            // Limpiar archivos antiguos
            File[] files = directory.listFiles((dir, name) -> name.startsWith(filePrefix) && name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        Log.w(TAG, "Failed to delete old file: " + file.getName());
                    }
                }
            }

            // Crear nuevo archivo
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = filePrefix + "_" + timeStamp + ".png";
            File file = new File(directory, imageFileName);

            // Guardar la imagen
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();

            Log.d(TAG, "Image saved successfully: " + file.getAbsolutePath());
            return file.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage(), e);
            return null;
        } finally {
            // Cerrar streams
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean deleteImageFile(Context context, String filePath) {
        if (filePath == null) return false;
        
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image file: " + e.getMessage());
            return false;
        }
    }

    public static void cleanupOldFiles(Context context, String featurePath, String filePrefix) {
        try {
            File directory = new File(context.getFilesDir(), featurePath);
            if (directory.exists()) {
                File[] files = directory.listFiles((dir, name) -> name.startsWith(filePrefix) && name.endsWith(".png"));
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up old files: " + e.getMessage());
        }
    }
}