package pl.coopsoft.szambelan.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.coopsoft.szambelan.R

@Composable
fun DayNightSwitch(
    themeMode: MutableState<ThemeMode>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Image(
                painter = painterResource(themeMode.value.icon),
                contentDescription = stringResource(R.string.color_mode),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun DayNightSwitchPreview() {
    val themeMode = remember { mutableStateOf(ThemeMode.LIGHT) }
    DayNightSwitch(
        themeMode = themeMode,
        onClick = { themeMode.value = themeMode.value.next() }
    )
}