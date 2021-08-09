package pl.coopsoft.szambelan

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val FULL_CONTAINER = 6.0 // [m^3]
        private const val MS_IN_HOUR = 3600000
        private const val COLOR_NORMAL = Color.WHITE
        private const val HOURS_WARN1 = 72
        private const val HOURS_WARN2 = 48
        private val COLOR_WARN1 = Color.rgb(255, 128, 0)
        private val COLOR_WARN2 = Color.rgb(255, 0, 0)
    }

    val editTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            refreshCalculation()
        }
    }

    val prevEmptyActions = MutableLiveData<String>()
    val prevMainMeter = MutableLiveData<String>()
    val prevGardenMeter = MutableLiveData<String>()
    val currentMainMeter = MutableLiveData<String>()
    val currentGardenMeter = MutableLiveData<String>()
    val waterUsage = MutableLiveData<CharSequence>()
    val daysLeft = MutableLiveData<String>()
    val daysLeftColor = MutableLiveData(COLOR_NORMAL)

    private var emptyActions = mutableListOf<MeterStates>()

    init {
        loadEditValues()
        loadMeterStates()
        showMeterStates()
        refreshCalculation()
    }

    private fun context(): Context = getApplication<Application>()

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

    fun saveEditValues() {
        Persistence.putDouble(
            context(), Persistence.PREF_OLD_MAIN,
            Utils.toDouble(prevMainMeter.value.orEmpty())
        )
        Persistence.putDouble(
            context(), Persistence.PREF_OLD_GARDEN,
            Utils.toDouble(prevGardenMeter.value.orEmpty())
        )
        Persistence.putDouble(
            context(), Persistence.PREF_CURRENT_MAIN,
            Utils.toDouble(currentMainMeter.value.orEmpty())
        )
        Persistence.putDouble(
            context(), Persistence.PREF_CURRENT_GARDEN,
            Utils.toDouble(currentGardenMeter.value.orEmpty())
        )
    }

    fun refreshCalculation() {
        val prevMain = Utils.toDouble(prevMainMeter.value.orEmpty())
        val prevGarden = Utils.toDouble(prevGardenMeter.value.orEmpty())
        val currentMain = Utils.toDouble(currentMainMeter.value.orEmpty())
        val currentGarden = Utils.toDouble(currentGardenMeter.value.orEmpty())
        val usage = currentMain - prevMain - (currentGarden - prevGarden)
        val usageText = String.format(Locale.GERMANY, "%1$.2f", usage)
        val percentage = (usage * 100.0 / FULL_CONTAINER).roundToInt()
        waterUsage.value =
            Html.fromHtml(
                "$usageText m<sup><small>3</small></sup>  ($percentage%)",
                Html.FROM_HTML_MODE_LEGACY
            )

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

    private fun saveMeterStates() {
        val data = emptyActions.joinToString(separator = "\n")
        Persistence.putString(context(), Persistence.PREF_EMPTY_ACTIONS, data)
    }

    private fun showMeterStates() {
        val text = StringBuilder()
        for (i in emptyActions.indices) {
            val line =
                emptyActions[i].toVisibleString(context(), if (i > 0) emptyActions[i - 1] else null)
            if (text.isNotEmpty()) {
                text.append("\n")
            }
            text.append(line)
        }
//        val text = emptyActions.joinToString(
//            separator = "\n",
//            transform = { it.toVisibleString() }
//        )
        prevEmptyActions.value =
            if (text.isNotEmpty()) text.toString() else context().getString(R.string.no_data)
    }

    fun emptyTank() {
        prevMainMeter.value = currentMainMeter.value
        prevGardenMeter.value = currentGardenMeter.value
        refreshCalculation()

        val meterStates = MeterStates(
            date = System.currentTimeMillis(),
            mainMeter = Utils.toDouble(prevMainMeter.value.orEmpty()),
            gardenMeter = Utils.toDouble(prevGardenMeter.value.orEmpty())
        )
        emptyActions.add(meterStates)
        saveMeterStates()
        showMeterStates()
    }
}