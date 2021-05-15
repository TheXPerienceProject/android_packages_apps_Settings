/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2021 The XPerience Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SELinux;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.io.BufferedReader;
import java.io.StringBufferInputStream;
import java.io.InputStreamReader;
import java.lang.Runtime;

public class SelinuxStatusPreferenceController extends BasePreferenceController {

    private static final String TAG = "SelinuxStatusCtrl";

    public SelinuxStatusPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

	public boolean isSeLinuxEnforcing() {
		StringBuffer output = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec("getenforce");
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line);
			}
		} catch (Exception e) {
			Log.e(TAG, "OS does not support getenforce");
			// If getenforce is not available to the device, assume the device is not enforcing
			e.printStackTrace();
			return false;
		}
		String response = output.toString();
		if ("Enforcing".equals(response)) {
			return true;
		} else if ("Permissive".equals(response)) {
			return false;
		} else {
			Log.e(TAG, "getenforce returned unexpected value, unable to determine selinux!");
			// If getenforce is modified on this device, assume the device is not enforcing
			return false;
		}
	}

	public boolean isSELinuxEnforced() {
		if (isSeLinuxEnforcing() == true){
		return true;
		}
		return false;
	}

    @Override
    public CharSequence getSummary() {
        if (!SELinux.isSELinuxEnabled()) {
            return (CharSequence) mContext.getString(R.string.selinux_status_disabled);
        } else if (isSELinuxEnforced() == false) {
            return (CharSequence) mContext.getString(R.string.selinux_status_permissive);
        } else {
            return (CharSequence) mContext.getString(R.string.selinux_status_enforcing);
        }
    }

}
