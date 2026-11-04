package com.spacewalls.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** AMOLED-first palette: true black canvas, silver type, cosmic-blue accents. */
object SpaceColors {
    val Black = Color(0xFF000000)
    val Void = Color(0xFF05060A)
    val Surface = Color(0xFF0A0C12)
    val SurfaceElevated = Color(0xFF11141C)
    val Glass = Color(0x14FFFFFF)
    val GlassStrong = Color(0x1FFFFFFF)
    val Border = Color(0x1AFFFFFF)
    val BorderStrong = Color(0x33FFFFFF)
    val CosmicBlue = Color(0xFF4F8CFF)
    val CosmicBlueDeep = Color(0xFF1B3A78)
    val Silver = Color(0xFFC9D1E3)
    val TextPrimary = Color(0xFFF4F7FF)
    val TextSecondary = Color(0xFF98A1B7)
    val TextTertiary = Color(0xFF6B7488)
    val Success = Color(0xFF4ADE80)
    val Warning = Color(0xFFFBBF24)
}

private val SpaceColorScheme = darkColorScheme(
    primary = SpaceColors.CosmicBlue,
    onPrimary = Color.White,
    primaryContainer = SpaceColors.CosmicBlueDeep,
    onPrimaryContainer = SpaceColors.TextPrimary,
    secondary = SpaceColors.Silver,
    onSecondary = SpaceColors.Black,
    background = SpaceColors.Black,
    onBackground = SpaceColors.TextPrimary,
    surface = SpaceColors.Black,
    onSurface = SpaceColors.TextPrimary,
    surfaceVariant = SpaceColors.Surface,
    onSurfaceVariant = SpaceColors.TextSecondary,
    surfaceContainer = SpaceColors.SurfaceElevated,
    outline = SpaceColors.Border,
    outlineVariant = SpaceColors.Border,
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

private val SpaceTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
)

@Composable
fun SpaceWallsTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()

    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = if (dark) SpaceColorScheme else SpaceColorScheme,
        typography = SpaceTypography,
        content = content
    )
}
