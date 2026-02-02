/*
 * Copyright (C) 2021 Project Radiant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xperience.widget;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */
/**
 * An {@link ImageView} that automatically loads the device's current
 * wallpaper and renders it with a Gaussian blur.
 *
 * <p>Requires API 31+ for {@link RenderEffect}. The blur radius (18 px)
 * is tuned so the scrim overlay on top remains fully legible.
 *
 * <p>Usage in XML:
 * <pre>
 *   &lt;com.xperience.widget.WallpaperBlurView
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"
 *       android:scaleType="centerCrop" /&gt;
 * </pre>
 */
public class WallpaperBlurView extends ImageView {

    private static final float BLUR_RADIUS = 18f;

    private final Context mContext;

    public WallpaperBlurView(Context context) {
        super(context);
        mContext = context;
        applyBlur();
    }

    public WallpaperBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        applyBlur();
    }

    public WallpaperBlurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        applyBlur();
    }

    /**
     * Called when the view has been measured and laid out.
     * We load the wallpaper here (instead of the constructor) because
     * {@link WallpaperManager#getDrawable()} needs the view to have
     * a size on some devices.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        WallpaperManager wm = WallpaperManager.getInstance(mContext);
        setImageDrawable(wm.getDrawable());
    }

    private void applyBlur() {
        setRenderEffect(
                RenderEffect.createBlurEffect(
                        BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.CLAMP));
    }
}
