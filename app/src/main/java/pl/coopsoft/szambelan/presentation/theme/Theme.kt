package pl.coopsoft.szambelan.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val lightColors = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White
)

private val darkColors = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White
)

@Composable
fun MainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorSchemeColors = if (darkTheme) darkColors else lightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorSchemeColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorSchemeColors,
//        typography = Typography,
//        shapes = Shapes,
        content = content
    )
}