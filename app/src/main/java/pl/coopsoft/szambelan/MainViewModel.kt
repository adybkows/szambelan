package pl.coopsoft.szambelan

import android.app.Application
import android.content.Context
import android.text.Editable
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import pl.coopsoft.szambelan.communication.RemoteStorageHelper
import pl.coopsoft.szambelan.models.DataModel
import pl.coopsoft.szambelan.models.MeterStates
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

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

    var userId = ""
    val prevEmptyActions = mutableStateOf("")
    val prevMainMeter = mutableStateOf("")
    val prevGardenMeter = mutableStateOf("")
    val currentMainMeter = mutableStateOf("")
    val currentGardenMeter = mutableStateOf("")
    val waterUsage = mutableStateOf(AnnotatedString(""))
    val daysLeft = mutableStateOf("")
    val daysLeftColor = mutableStateOf(COLOR_NORMAL)

    private val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    private val wrongDecimalSeparator = if (decimalSeparator == '.') ',' else '.'

    private var emptyActions = mutableListOf<MeterStates>()

    private fun context(): Context = getApplication<Application>()

    fun loadAllData() {
        loadUserId()
        loadEditValues()
        loadMeterStates()
        showMeterStates()
        refreshCalculation()
    }

    private fun loadUserId() {
        userId = Persistence.getString(context(), Persistence.PREF_USER_ID, "")
        if (userId.isEmpty()) {
            userId = UUID.randomUUID().toString()
            Persistence.putString(context(), Persistence.PREF_USER_ID, userId)
        }
    }

    private fun loadEditValues() {
        prevMainMeter.value =
            Utils.toString(Persistence.getDouble(context(), Persistence.PREF_OLD_MAIN))
        prevGardenMeter.value =
            Utils.toString(Persistence.getDouble(context(), Persistence.PREF_OLD_GARDEN))
        currentMainMeter.value =
            Utils.toString(Persistence.getDouble(context(), Persistence.PREF_CURRENT_MAIN))
        currentGardenMeter.value =
            Utils.toString(Persistence.getDouble(context(), Persistence.PREF_CURRENT_GARDEN))
    }

    fun downloadFromRemoteStorage(done: (Boolean) -> Unit) {
        RemoteStorageHelper.downloadData(userId) { data ->
            if (data != null) {
                Log.i(TAG, "Data from remote storage downloaded successfully")
                prevMainMeter.value = Utils.toString(data.prevMainMeter)
                prevGardenMeter.value = Utils.toString(data.prevGardenMeter)
                currentMainMeter.value = Utils.toString(data.currentMainMeter)
                currentGardenMeter.value = Utils.toString(data.currentGardenMeter)
                emptyActions = data.emptyActions.toMutableList()
                done(true)
            } else {
                Log.e(TAG, "Download from remote storage failed")
                done(false)
            }
        }
    }

    fun uploadToRemoteStorage(done: (Boolean) -> Unit) {
        val data = DataModel(
            prevMainMeter = Utils.toDouble(prevMainMeter.value),
            prevGardenMeter = Utils.toDouble(prevGardenMeter.value),
            currentMainMeter = Utils.toDouble(currentMainMeter.value),
            currentGardenMeter = Utils.toDouble(currentGardenMeter.value),
            emptyActions = emptyActions
        )
        RemoteStorageHelper.uploadData(data, userId) { t->
            if (t != null) {
                Log.e(TAG, "Upload failed: $t")
                done(false)
            } else {
                Log.i(TAG, "Data uploaded successfully to remote storage")
                done(true)
            }
        }
    }

    fun saveEditValues() {
        Persistence.putDouble(
            context(), Persistence.PREF_OLD_MAIN,
            Utils.toDouble(prevMainMeter.value)
        )
        Persistence.putDouble(
            context(), Persistence.PREF_OLD_GARDEN,
            Utils.toDouble(prevGardenMeter.value)
        )
        Persistence.putDouble(
            context(), Persistence.PREF_CURRENT_MAIN,
            Utils.toDouble(currentMainMeter.value)
        )
        Persistence.putDouble(
            context(), Persistence.PREF_CURRENT_GARDEN,
            Utils.toDouble(currentGardenMeter.value)
        )
    }

    fun refreshCalculation() {
        val prevMain = Utils.toDouble(prevMainMeter.value)
        val prevGarden = Utils.toDouble(prevGardenMeter.value)
        val currentMain = Utils.toDouble(currentMainMeter.value)
        val currentGarden = Utils.toDouble(currentGardenMeter.value)
        val usage = currentMain - prevMain - (currentGarden - prevGarden)
        val usageText = String.format(Locale.getDefault(), "%1$.2f", usage)
        val percentage = (usage * 100.0 / FULL_CONTAINER).roundToInt()
        val superScriptStyle =
            SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 16.sp)
        waterUsage.value =
            if (usage < 0)
                AnnotatedString("")
            else
                buildAnnotatedString {
                    append("$usageText m")
                    withStyle(superScriptStyle) { append("3") }
                    append("  ($percentage%)")
                }

        if (emptyActions.isNotEmpty() && percentage > 0) {
            val lastEmptyAction = emptyActions.last()
            val hours =
                (System.currentTimeMillis() - lastEmptyAction.date) / MS_IN_HOUR
            val hoursTotal = hours * 100 / percentage
            val hoursLeft = hoursTotal - hours
            daysLeft.value = Utils.toDaysHours(context(), hoursLeft)
            daysLeftColor.value = when {
                hoursLeft < HOURS_WARN2 -> COLOR_WARN2
                hoursLeft < HOURS_WARN1 -> COLOR_WARN1
                else -> COLOR_NORMAL
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

    fun updateDecimalSeparator(s: String) =
        s.replace(wrongDecimalSeparator, decimalSeparator)

    private fun loadMeterStates() {
        val lines = Persistence.getString(context(), Persistence.PREF_EMPTY_ACTIONS, "")
            .split('\n')
            .filterNot { it.isEmpty() }
        if (lines.isEmpty()) {
            emptyActions.clear()
        } else {
            emptyActions = lines.map { MeterStates.fromString(it) }.toMutableList()
        }
    }

    fun saveMeterStates() {
        val data = emptyActions.joinToString(separator = "\n")
        Persistence.putString(context(), Persistence.PREF_EMPTY_ACTIONS, data)
    }

    fun showMeterStates() {
        val text = StringBuilder()
        for (i in emptyActions.indices) {
            val line =
                emptyActions[i].toVisibleString(context(), if (i > 0) emptyActions[i - 1] else null)
            if (text.isNotEmpty()) {
                text.append("\n")
            }
            text.append(line)
        }
        prevEmptyActions.value =
            if (text.isNotEmpty())
                limitLines(text.toString(), MAX_METER_STATES)
            else
                context().getString(R.string.no_data)
    }

    private fun limitLines(s: String, maxLines: Int): String {
        val lines = s.split('\n')
        if (lines.size <= maxLines) {
            return s
        }
        val lastLines = lines.subList(lines.size - maxLines, lines.size)
        return lastLines.joinToString("\n")
    }

    fun emptyTank() {
        prevMainMeter.value = currentMainMeter.value
        prevGardenMeter.value = currentGardenMeter.value
        refreshCalculation()

        val meterStates = MeterStates(
            date = System.currentTimeMillis(),
            mainMeter = Utils.toDouble(prevMainMeter.value),
            gardenMeter = Utils.toDouble(prevGardenMeter.value)
        )
        emptyActions.add(meterStates)
        saveMeterStates()
        showMeterStates()
    }
}