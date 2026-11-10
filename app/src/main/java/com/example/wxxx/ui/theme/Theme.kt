package com.example.wxxx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.wxxx.AppThemeMode

val LocalIsDarkTheme = compositionLocalOf { false }
val LocalAppThemeMode = compositionLocalOf { AppThemeMode.MODE_LIGHT }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    secondary = Color(0xFFABEE4F),
    tertiary = Color(0xFF8AB4F8),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10B981),
    secondary = Color(0xFFABEE4F),
    tertiary = Color(0xFF8AB4F8),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

/**
 * 🌟 [Aura 19.0 全局主题容器与深色模式自适应中枢]
 */
@Composable
fun AuraTheme(
    themeMode: AppThemeMode = AppThemeMode.MODE_LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.MODE_LIGHT -> false
        AppThemeMode.MODE_DARK -> true
        AppThemeMode.MODE_SYSTEM -> isSystemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 🌟 系统沉浸式状态栏与导航栏图标自动随深浅主题智能切换，并同步原生窗口背景色杜绝半透明漏底白闪
    val context = LocalContext.current
    SideEffect {
        val window = (context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            window.decorView.setBackgroundColor(
                if (darkTheme) android.graphics.Color.parseColor("#121212")
                else android.graphics.Color.parseColor("#F8F6FA")
            )
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalAppThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
