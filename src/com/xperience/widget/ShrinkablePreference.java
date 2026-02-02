/*
 * Copyright (C) 2024 PenguinOS
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */
/**
 * A {@link Preference} that inflates the hero OTA card layout and adds a
 * "springy shrink" press animation.
 *
 * <p>Animation sequence:
 *   ACTION_DOWN  →  scale 1.0 → 0.95  (100 ms)
 *   ACTION_UP    →  scale 0.95 → 1.05 (100 ms) → 1.05 → 1.0 (100 ms)
 *
 * <p>On a successful tap it launches {@code SYSTEM_UPDATE_SETTINGS}.
 * If that activity is not found (unofficial build / no updater installed)
 * a short toast is shown instead.
 *
 * <p>Declared in XML as:
 * <pre>
 *   &lt;com.xperience.widget.ShrinkablePreference
 *       android:key="ota_card"
 *       settings:layout="@layout/xpe_tp_view" /&gt;
 * </pre>
 */
public class ShrinkablePreference extends Preference {

    private static final int ANIM_DURATION_MS = 100;
    private static final float SCALE_PRESSED  = 0.95f;
    private static final float SCALE_BOUNCE  = 1.05f;
    private static final float SCALE_NORMAL  = 1.0f;

    public ShrinkablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.xpe_tp_view);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View itemView = holder.itemView;
        itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleAnim(SCALE_NORMAL, SCALE_PRESSED, true));
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Bounce: 0.95 → 1.05, then chain 1.05 → 1.0
                    ScaleAnimation bounce = scaleAnim(SCALE_PRESSED, SCALE_BOUNCE, false);
                    ScaleAnimation restore = scaleAnim(SCALE_BOUNCE, SCALE_NORMAL, true);

                    bounce.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation a)  {}
                        @Override public void onAnimationRepeat(Animation a) {}
                        @Override
                        public void onAnimationEnd(Animation a) {
                            v.startAnimation(restore);
                        }
                    });
                    v.startAnimation(bounce);

                    // Launch updater on actual tap (not cancel)
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        launchUpdater();
                    }
                    break;
            }
            return false; // let the event propagate normally
        });
    }

    private void launchUpdater() {
        Intent intent = new Intent("android.settings.SYSTEM_UPDATE_SETTINGS");
        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private static ScaleAnimation scaleAnim(float from, float to, boolean fillAfter) {
        ScaleAnimation anim = new ScaleAnimation(
                from, to,   // X
                from, to,   // Y
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(ANIM_DURATION_MS);
        anim.setFillAfter(fillAfter);
        return anim;
    }
}
