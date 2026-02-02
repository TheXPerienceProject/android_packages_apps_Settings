/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package com.xperience.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.google.android.material.card.MaterialCardView;

public class DualColumnPreference extends Preference {

    private String mTitleLeft;
    private String mSummaryLeft;
    private int mIconLeftResId;
    private View.OnClickListener mLeftClickListener;

    private String mTitleRight;
    private String mSummaryRight;
    private int mIconRightResId;
    private View.OnClickListener mRightClickListener;

    public DualColumnPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.xpe_card_dual_column);
        setSelectable(false); // La preferencia principal no es seleccionable
    }

    public void setLeftColumn(String title, String summary, int iconResId) {
        mTitleLeft = title;
        mSummaryLeft = summary;
        mIconLeftResId = iconResId;
        notifyChanged();
    }

    public void setRightColumn(String title, String summary, int iconResId) {
        mTitleRight = title;
        mSummaryRight = summary;
        mIconRightResId = iconResId;
        notifyChanged();
    }

    public void setLeftClickListener(View.OnClickListener listener) {
        mLeftClickListener = listener;
        notifyChanged();
    }

    public void setRightClickListener(View.OnClickListener listener) {
        mRightClickListener = listener;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Deshabilitar el click en el contenedor principal
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        // Obtener las cards
        MaterialCardView cardLeft = (MaterialCardView) holder.findViewById(R.id.card_left);
        MaterialCardView cardRight = (MaterialCardView) holder.findViewById(R.id.card_right);

        // Configurar columna izquierda
        ImageView iconLeft = (ImageView) holder.findViewById(R.id.icon_left);
        TextView titleLeft = (TextView) holder.findViewById(R.id.title_left);
        TextView summaryLeft = (TextView) holder.findViewById(R.id.summary_left);

        if (iconLeft != null && mIconLeftResId != 0) {
            iconLeft.setImageResource(mIconLeftResId);
            iconLeft.setVisibility(View.VISIBLE);
        } else if (iconLeft != null) {
            iconLeft.setVisibility(View.GONE);
        }

        if (titleLeft != null) {
            titleLeft.setText(mTitleLeft);
        }
        if (summaryLeft != null) {
            summaryLeft.setText(mSummaryLeft);
        }

        // Configurar columna derecha
        ImageView iconRight = (ImageView) holder.findViewById(R.id.icon_right);
        TextView titleRight = (TextView) holder.findViewById(R.id.title_right);
        TextView summaryRight = (TextView) holder.findViewById(R.id.summary_right);

        if (iconRight != null && mIconRightResId != 0) {
            iconRight.setImageResource(mIconRightResId);
            iconRight.setVisibility(View.VISIBLE);
        } else if (iconRight != null) {
            iconRight.setVisibility(View.GONE);
        }

        if (titleRight != null) {
            titleRight.setText(mTitleRight);
        }
        if (summaryRight != null) {
            summaryRight.setText(mSummaryRight);
        }

        // Hacer las cards clickeables individualmente
        if (cardLeft != null) {
            cardLeft.setClickable(true);
            cardLeft.setFocusable(true);
            cardLeft.setOnClickListener(mLeftClickListener);

            // Agregar efecto ripple de Material Design
            cardLeft.setRippleColor(getContext().getResources().getColorStateList(
                android.R.color.system_accent1_100, getContext().getTheme()));
        }

        if (cardRight != null) {
            cardRight.setClickable(true);
            cardRight.setFocusable(true);
            cardRight.setOnClickListener(mRightClickListener);

            // Agregar efecto ripple de Material Design
            cardRight.setRippleColor(getContext().getResources().getColorStateList(
                android.R.color.system_accent1_100, getContext().getTheme()));
        }
    }
}
