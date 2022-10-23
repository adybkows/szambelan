package pl.coopsoft.szambelan.presentation.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import pl.coopsoft.szambelan.R

enum class ThemeMode(@DrawableRes val icon: Int) {
    AUTO(R.drawable.ic_auto),
    LIGHT(R.drawable.ic_day),
    DARK(R.drawable.ic_night);

    fun toInt(): Int =
        this.ordinal

    fun next(): ThemeMode =
        ThemeMode.values().getOrElse(this.ordinal + 1) { AUTO }
}

fun Int.toThemeMode(): ThemeMode =
    ThemeMode.values().getOrElse(this) { ThemeMode.AUTO }

@Composable
fun ThemeMode.isDarkTheme(): Boolean =
    when(this) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
