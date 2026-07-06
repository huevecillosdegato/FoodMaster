package com.foodmaster.app.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Green = Color(0xFF2E7D32)
private val GreenDark = Color(0xFF1B5E20)
private val Amber = Color(0xFFFFA000)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = Amber,
    tertiary = GreenDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Amber,
    tertiary = Color(0xFFA5D6A7),
)

@Composable
fun FoodMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
