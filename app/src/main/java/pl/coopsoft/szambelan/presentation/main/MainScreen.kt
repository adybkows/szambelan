package pl.coopsoft.szambelan.presentation.main

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import androidx.navigation.NavController
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import pl.coopsoft.szambelan.presentation.dialogs.DialogData
import pl.coopsoft.szambelan.presentation.dialogs.DisplayDialogs
import pl.coopsoft.szambelan.presentation.theme.MainTheme

@Composable
fun MainScreen(
    loggedIn: Boolean,
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
    daysSince: MutableState<String>,
    daysLeft: MutableState<String>,
    daysLeftColor: MutableState<Color>,
    emptyTankClicked: () -> Unit,
    logInOutClicked: () -> Unit,
    downloadClicked: () -> Unit,
    uploadClicked: () -> Unit,
    formattingUtils: FormattingUtils,
    dialogs: SnapshotStateList<DialogData>,
    dismissDialog: (DialogData, (() -> Unit)?) -> Unit
) {
    MainTheme {
        val dialogList = remember { dialogs }
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    if (loggedIn) {
                        Button(
                            onClick = downloadClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = stringResource(R.string.download),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        SimpleTextButton(
                            text = stringResource(
                                if (loggedIn) R.string.log_out else R.string.log_in
                            ).uppercase(),
                            textColor = if (loggedIn) Color.White else Color.Black,
                            bgColor = if (loggedIn) Color.Red else Color.Green,
                            onClick = logInOutClicked
                        )
                    }
                    if (loggedIn) {
                        Button(
                            onClick = uploadClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_upload),
                                contentDescription = stringResource(R.string.upload),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                            )
                        }
                    }
                }
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
                    isLast = false,
                    enableEdition = false,
                    formattingUtils = formattingUtils
                )
                MeterStateBlock(
                    title = R.string.current_state,
                    mainName = R.string.main_meter,
                    mainValue = currentMainMeter,
                    onMainValueChange = onCurrentMainMeterChange,
                    secondName = R.string.garden_meter,
                    secondValue = currentGardenMeter,
                    onSecondValueChange = onCurrentGardenMeterChange,
                    isLast = true,
                    enableEdition = true,
                    formattingUtils = formattingUtils
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.days_passed),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = daysSince.value,
                            fontSize = 24.sp
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
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
            DisplayDialogs(dialogList, dismissDialog)
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
    isLast: Boolean,
    enableEdition: Boolean,
    formattingUtils: FormattingUtils
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
                isLast = false,
                enableEdition = enableEdition,
                formattingUtils = formattingUtils
            )
            MeterState(
                modifier = Modifier.padding(start = 16.dp),
                name = secondName,
                text = secondValue,
                onValueChange = onSecondValueChange,
                endPadding = 0.dp,
                isLast = isLast,
                enableEdition = enableEdition,
                formattingUtils = formattingUtils
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MeterState(
    modifier: Modifier = Modifier,
    endPadding: Dp,
    @StringRes name: Int,
    text: MutableState<String>,
    onValueChange: (String) -> Unit,
    isLast: Boolean,
    enableEdition: Boolean,
    formattingUtils: FormattingUtils
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Text(
            text = stringResource(name)
        )
        OutlinedTextField(
            modifier = Modifier.padding(top = 8.dp, end = endPadding),
            enabled = enableEdition,
            value = text.value,
            onValueChange = { v ->
                val filtered = formattingUtils.maxOneDot(v.filter {
                    formattingUtils.isDigitOrDot(it)
                })
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
fun SimpleTextButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor
        )
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel,
    formattingUtils: FormattingUtils
) {
    MainScreen(
        loggedIn = viewModel.loggedIn.value,
        prevEmptyActions = viewModel.prevEmptyActions,
        prevMainMeter = viewModel.prevMainMeter,
        onPrevMainMeterChange = {
            viewModel.prevMainMeter.value = viewModel.updateDecimalSeparator(it)
            viewModel.refreshCalculation()
        },
        prevGardenMeter = viewModel.prevGardenMeter,
        onPrevGardenMeterChange = {
            viewModel.prevGardenMeter.value = viewModel.updateDecimalSeparator(it)
            viewModel.refreshCalculation()
        },
        currentMainMeter = viewModel.currentMainMeter,
        onCurrentMainMeterChange = {
            viewModel.currentMainMeter.value = viewModel.updateDecimalSeparator(it)
            viewModel.refreshCalculation()
        },
        currentGardenMeter = viewModel.currentGardenMeter,
        onCurrentGardenMeterChange = {
            viewModel.currentGardenMeter.value = viewModel.updateDecimalSeparator(it)
            viewModel.refreshCalculation()
        },
        waterUsage = viewModel.waterUsage,
        daysSince = viewModel.daysSince,
        daysLeft = viewModel.daysLeft,
        daysLeftColor = viewModel.daysLeftColor,
        emptyTankClicked = viewModel::emptyTankClicked,
        logInOutClicked = { viewModel.logInOutClicked(navController) },
        downloadClicked = viewModel::downloadClicked,
        uploadClicked = viewModel::uploadClicked,
        formattingUtils = formattingUtils,
        dialogs = viewModel.dialogs,
        dismissDialog = viewModel::dismissDialog
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        true,
        mutableStateOf("ABCD\nABCD"),
        mutableStateOf("123,45"), {}, mutableStateOf("43,21"), {},
        mutableStateOf("123,45"), {}, mutableStateOf("43,21"), {},
        mutableStateOf(AnnotatedString("90%")), mutableStateOf("10"),
        mutableStateOf("1"), mutableStateOf(Color.Red),
        {}, {}, {}, {},
        FormattingUtils(), mutableStateListOf(), { _, _ -> }
    )
}
