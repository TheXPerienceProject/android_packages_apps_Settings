/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2018 The XPerience Project
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
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import android.util.Log;
import android.view.View;

import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;

public class XPerienceBranchDialogController {

  @VisibleForTesting
  private static final int ACTIVITY_TRIGGER_COUNT = 5;
  @VisibleForTesting
  private static final String XPERIENCE_PROPERTY = "ro.xpe.cafbranch";
  @VisibleForTesting
  static final int XPERIENCE_BRANCH_VALUE_ID = R.id.xperience_branch_value;

  private final UserManager mUserManager;
  private final Context mContext;
  private final FirmwareVersionDialogFragment mDialog;
  private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];

  private RestrictedLockUtils.EnforcedAdmin mFunDisallowedAdmin;
  private boolean mFunDisallowedBySystem;

  public XPerienceBranchDialogController(FirmwareVersionDialogFragment dialog) {
      mDialog = dialog;
      mContext = dialog.getContext();
      mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
  }

  /**
   * Populates the XPerience branch field in the dialog.
   */
  public void initialize() {
      initializeAdminPermissions();
      mDialog.setText(XPERIENCE_BRANCH_VALUE_ID, SystemProperties.get(XPERIENCE_PROPERTY,
              mContext.getResources().getString(R.string.device_info_default)));
  }

  /**
   * Copies the array onto itself to remove the oldest hit.
   */
  void arrayCopy() {
      System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
  }

  void initializeAdminPermissions() {
      mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(
              mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
      mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(
              mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
  }

}
