package pl.coopsoft.szambelan.ui.main

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.Utils
import pl.coopsoft.szambelan.ui.theme.MainTheme

@Composable
fun MainActivityScreen(
    prevEmptyActions: MutableState<String>,
    prevMainMeter: MutableState<String>,
    onPrevMainMeterChange: (String) -> Unit,
    prevGardenMeter: MutableState<String>,
    onPrevGardenMeterChange: (String) -> Unit,
    currentMainMeter: MutableState<String>,
    onCurrentMainMeterChange: (String) -> Unit,
    currentGardenMeter: MutableState<String>,
    onCurrentGardenMeterChange: (String) -> Unit,
    waterUsage: MutableState<AnnotatedString>,
    daysLeft: MutableState<String>,
    daysLeftColor: MutableState<Color>,
    emptyTankClicked: () -> Unit
) {
    MainTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.prev_empty_actions),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prevEmptyActions.value
                )
                MeterStateBlock(
                    title = R.string.last_state,
                    mainName = R.string.main_meter,
                    mainValue = prevMainMeter,
                    onMainValueChange = onPrevMainMeterChange,
                    secondName = R.string.garden_meter,
                    secondValue = prevGardenMeter,
                    onSecondValueChange = onPrevGardenMeterChange,
                    isLast = false
                )
                MeterStateBlock(
                    title = R.string.current_state,
                    mainName = R.string.main_meter,
                    mainValue = currentMainMeter,
                    onMainValueChange = onCurrentMainMeterChange,
                    secondName = R.string.garden_meter,
                    secondValue = currentGardenMeter,
                    onSecondValueChange = onCurrentGardenMeterChange,
                    isLast = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                        Text(
                            text = stringResource(R.string.output_result),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = waterUsage.value,
                            fontSize = 24.sp
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.days_left),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = daysLeft.value,
                            fontSize = 24.sp,
                            color = daysLeftColor.value
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { emptyTankClicked() }
                    ) {
                        Text(text = stringResource(R.string.empty_tank).uppercase())
                    }
                }
            }
        }
    }
}

@Composable
fun MeterStateBlock(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes mainName: Int,
    mainValue: MutableState<String>,
    onMainValueChange: (String) -> Unit,
    @StringRes secondName: Int,
    secondValue: MutableState<String>,
    onSecondValueChange: (String) -> Unit,
    isLast: Boolean
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            text = stringResource(title),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier.padding(top = 16.dp)) {
            MeterState(
                modifier = Modifier.fillMaxWidth(0.5f),
                name = mainName,
                text = mainValue,
                onValueChange = onMainValueChange,
                endPadding = 16.dp,
                isLast = false
            )
            MeterState(
                modifier = Modifier.padding(start = 16.dp),
                name = secondName,
                text = secondValue,
                onValueChange = onSecondValueChange,
                endPadding = 0.dp,
                isLast = isLast
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MeterState(
    modifier: Modifier = Modifier,
    endPadding: Dp,
    @StringRes name: Int,
    text: MutableState<String>,
    onValueChange: (String) -> Unit,
    isLast: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Text(
            text = stringResource(name)
        )
        OutlinedTextField(
            modifier = Modifier.padding(top = 8.dp, end = endPadding),
            value = text.value,
            onValueChange = { v ->
                val filtered = Utils.maxOneDot(v.filter { Utils.isDigitOrDot(it) })
                if (filtered != text.value) {
                    text.value = filtered
                    onValueChange(filtered)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                autoCorrect = false,
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )
    }
}

@Composable
fun StyledText(
    text: CharSequence, @ColorInt color: Int, sizeSp: Float, modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(color)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
            }
        },
        update = {
            it.text = text
        }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivityScreen(
        mutableStateOf("ABCD\nABCD"),
        mutableStateOf("123,45"), {}, mutableStateOf("43,21"), {},
        mutableStateOf("123,45"), {}, mutableStateOf("43,21"), {},
        mutableStateOf(AnnotatedString("90%")), mutableStateOf("1"),
        mutableStateOf(Color.Red), {}
    )
}
