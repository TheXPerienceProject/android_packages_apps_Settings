/*
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.deviceinfo.xperience.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = XperienceColors.PrimaryRed,
    onPrimary = Color.White,
    background = XperienceColors.DeepBlack,
    surface = XperienceColors.SurfaceGrey,
    onBackground = XperienceColors.TextPrimary,
    onSurface = XperienceColors.TextPrimary,
    surfaceVariant = XperienceColors.CardBackground,
    onSurfaceVariant = XperienceColors.TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = XperienceColors.PrimaryRed,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.Gray
)

@Composable
fun XperienceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}
