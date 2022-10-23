package pl.coopsoft.szambelan.presentation.main

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import pl.coopsoft.szambelan.core.utils.Persistence
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.model.MeterStates
import pl.coopsoft.szambelan.domain.usecase.login.LogOutUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.DownloadUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.UploadUseCase
import pl.coopsoft.szambelan.presentation.NavScreens
import pl.coopsoft.szambelan.presentation.dialogs.DialogData
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val downloadUseCase: DownloadUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val persistence: Persistence,
    private val uploadUseCase: UploadUseCase,
    private val formattingUtils: FormattingUtils
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
        private const val FULL_CONTAINER = 6.0 // [m^3]
        private const val MS_IN_HOUR = 3600000
        private const val HOURS_WARN1 = 72
        private const val HOURS_WARN2 = 48
        private const val MAX_METER_STATES = 5
        private val COLOR_NORMAL = Color.White
        private val COLOR_WARN1 = Color(0xffff8000)
        private val COLOR_WARN2 = Color.Red
    }

    val themeMode = mutableStateOf(
        persistence.getInt(application, Persistence.PREF_THEME_MODE, ThemeMode.AUTO.toInt())
            .toThemeMode()
    )
    val loggedIn = mutableStateOf(false)
    val prevEmptyActions = mutableStateOf("")
    val prevMainMeter = mutableStateOf("")
    val prevGardenMeter = mutableStateOf("")
    val currentMainMeter = mutableStateOf("")
    val currentGardenMeter = mutableStateOf("")
    val waterUsage = mutableStateOf(AnnotatedString(""))
    val daysSince = mutableStateOf("")
    val daysLeft = mutableStateOf("")
    val daysLeftColor = mutableStateOf(COLOR_NORMAL)
    val dialogs = mutableStateListOf<DialogData>()

    private val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    private val wrongDecimalSeparator = if (decimalSeparator == '.') ',' else '.'

    private var emptyActions = mutableListOf<MeterStates>()

    private fun context(): Context = getApplication<Application>()

    fun loadSavedData() {
        // load and show data from shared preferences
        loadEditValues()
        loadMeterStates()
        showMeterStates()
        refreshCalculation()
    }

    private fun loadEditValues() {
        prevMainMeter.value =
            formattingUtils.toString(persistence.getDouble(context(), Persistence.PREF_OLD_MAIN))
        prevGardenMeter.value =
            formattingUtils.toString(persistence.getDouble(context(), Persistence.PREF_OLD_GARDEN))
        currentMainMeter.value = formattingUtils.toString(
            persistence.getDouble(
                context(), Persistence.PREF_CURRENT_MAIN
            )
        )
        currentGardenMeter.value = formattingUtils.toString(
            persistence.getDouble(
                context(), Persistence.PREF_CURRENT_GARDEN
            )
        )
    }

    fun downloadClicked() {
        downloadUseCase.askAndDownload(this) { data ->
            prevMainMeter.value = formattingUtils.toString(data.prevMainMeter)
            prevGardenMeter.value = formattingUtils.toString(data.prevGardenMeter)
            currentMainMeter.value = formattingUtils.toString(data.currentMainMeter)
            currentGardenMeter.value = formattingUtils.toString(data.currentGardenMeter)
            emptyActions = data.emptyActions.toMutableList()

            showMeterStates()
            refreshCalculation()
            saveEditValues()
            saveMeterStates()
        }
    }

    fun uploadClicked() {
        val data = DataModel(
            prevMainMeter = formattingUtils.toDouble(prevMainMeter.value),
            prevGardenMeter = formattingUtils.toDouble(prevGardenMeter.value),
            currentMainMeter = formattingUtils.toDouble(currentMainMeter.value),
            currentGardenMeter = formattingUtils.toDouble(currentGardenMeter.value),
            emptyActions = emptyActions
        )
        uploadUseCase.askAndUpload(this, data)
    }

    fun saveEditValues() {
        persistence.putDouble(
            context(), Persistence.PREF_OLD_MAIN, formattingUtils.toDouble(prevMainMeter.value)
        )
        persistence.putDouble(
            context(), Persistence.PREF_OLD_GARDEN, formattingUtils.toDouble(prevGardenMeter.value)
        )
        persistence.putDouble(
            context(),
            Persistence.PREF_CURRENT_MAIN,
            formattingUtils.toDouble(currentMainMeter.value)
        )
        persistence.putDouble(
            context(),
            Persistence.PREF_CURRENT_GARDEN,
            formattingUtils.toDouble(currentGardenMeter.value)
        )
    }

    fun refreshCalculation() {
        val prevMain = formattingUtils.toDouble(prevMainMeter.value)
        val prevGarden = formattingUtils.toDouble(prevGardenMeter.value)
        val currentMain = formattingUtils.toDouble(currentMainMeter.value)
        val currentGarden = formattingUtils.toDouble(currentGardenMeter.value)
        val usage = currentMain - prevMain - (currentGarden - prevGarden)
        val usageText = String.format(Locale.getDefault(), "%1$.2f", usage)
        val percentage = (usage * 100.0 / FULL_CONTAINER).roundToInt()
        val superScriptStyle =
            SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 16.sp)
        waterUsage.value = if (usage < 0) AnnotatedString("")
        else buildAnnotatedString {
            append("$usageText m")
            withStyle(superScriptStyle) { append("3") }
            append("  ($percentage%)")
        }

        if (emptyActions.isNotEmpty()) {
            val lastEmptyAction = emptyActions.last()
            val hours = (System.currentTimeMillis() - lastEmptyAction.date) / MS_IN_HOUR
            daysSince.value = formattingUtils.toDaysHours(context(), hours)
            if (percentage > 0) {
                val hoursTotal = hours * 100 / percentage
                val hoursLeft = hoursTotal - hours
                daysLeft.value = formattingUtils.toDaysHours(context(), hoursLeft)
                daysLeftColor.value = when {
                    hoursLeft < HOURS_WARN2 -> COLOR_WARN2
                    hoursLeft < HOURS_WARN1 -> COLOR_WARN1
                    else -> COLOR_NORMAL
                }
            } else {
                daysLeft.value = ""
            }
        } else {
            daysLeft.value = ""
        }
    }

    private fun updateDecimalSeparator(s: Editable?) {
        if (s != null) {
            val pos = s.indexOf(wrongDecimalSeparator)
            if (pos >= 0) {
                s.replace(pos, pos + 1, decimalSeparator.toString())
            }
        }
    }

    fun updateDecimalSeparator(s: String) = s.replace(wrongDecimalSeparator, decimalSeparator)

    private fun loadMeterStates() {
        val lines = persistence.getString(context(), Persistence.PREF_EMPTY_ACTIONS, "").split('\n')
            .filterNot { it.isEmpty() }
        if (lines.isEmpty()) {
            emptyActions.clear()
        } else {
            emptyActions = lines.map { MeterStates.fromString(it) }.toMutableList()
        }
    }

    private fun saveMeterStates() {
        val data = emptyActions.joinToString(separator = "\n")
        persistence.putString(context(), Persistence.PREF_EMPTY_ACTIONS, data)
    }

    private fun showMeterStates() {
        val text = StringBuilder()
        for (i in emptyActions.indices) {
            val line = emptyActions[i].toVisibleString(
                context(), formattingUtils, if (i > 0) emptyActions[i - 1] else null
            )
            if (text.isNotEmpty()) {
                text.append("\n")
            }
            text.append(line)
        }
        prevEmptyActions.value =
            if (text.isNotEmpty()) limitLines(text.toString(), MAX_METER_STATES)
            else context().getString(R.string.no_data)
    }

    private fun limitLines(s: String, maxLines: Int): String {
        val lines = s.split('\n')
        if (lines.size <= maxLines) {
            return s
        }
        val lastLines = lines.subList(lines.size - maxLines, lines.size)
        return lastLines.joinToString("\n")
    }

    fun emptyTankClicked() {
        displayDialog(
            DialogData(
                title = R.string.empty_tank,
                text = R.string.empty_tank_question,
                positiveButton = R.string.yes,
                negativeButton = R.string.cancel,
                positiveButtonCallback = ::emptyTheTank
            )
        )
    }

    private fun emptyTheTank() {
        prevMainMeter.value = currentMainMeter.value
        prevGardenMeter.value = currentGardenMeter.value
        refreshCalculation()

        val meterStates = MeterStates(
            date = System.currentTimeMillis(),
            mainMeter = formattingUtils.toDouble(prevMainMeter.value),
            gardenMeter = formattingUtils.toDouble(prevGardenMeter.value)
        )
        emptyActions.add(meterStates)
        saveMeterStates()
        showMeterStates()
    }

    fun logInOutClicked(navController: NavController) {
        if (!loggedIn.value) {
            navController.navigate(NavScreens.LOGIN)
        } else {
            logOutUseCase.logOut()
        }
    }

    fun nextThemeMode() {
        themeMode.value = themeMode.value.next()
        persistence.putInt(context(), Persistence.PREF_THEME_MODE, themeMode.value.toInt())
    }

    fun displayDialog(dialogData: DialogData) {
        dialogs.add(dialogData)
    }

    fun dismissDialog(dialogData: DialogData, next: (() -> Unit)? = null) {
        dialogs.remove(dialogData)
        if (next != null) {
            // wait a bit for recomposition
            viewModelScope.launch(Dispatchers.IO) {
                delay(10)
                viewModelScope.launch(Dispatchers.Main) {
                    next()
                }
            }
        }
    }

}