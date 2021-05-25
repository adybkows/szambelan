package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import pl.coopsoft.szambelan.databinding.ActivityMainBinding
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), TextWatcher {

    private companion object {
        private const val FULL_CONTAINER = 6.0 // [m^3]
        private const val MS_IN_HOUR = 3600000
        private const val COLOR_NORMAL = Color.WHITE
        private const val HOURS_WARN1 = 72
        private const val HOURS_WARN2 = 48
        private val COLOR_WARN1 = Color.rgb(255, 128, 0)
        private val COLOR_WARN2 = Color.rgb(255, 0, 0)
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var prevEmptyActions = mutableListOf<MeterStates>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        loadMeterStates()
        refreshMeterStates()
        refreshCalculation()

        binding.prevMainEditText.addTextChangedListener(this)
        binding.prevGardenEditText.addTextChangedListener(this)
        binding.currentMainEditText.addTextChangedListener(this)
        binding.currentGardenEditText.addTextChangedListener(this)

        binding.emptyTankButton.setOnClickListener { emptyTankClicked() }
    }

    override fun onDestroy() {
        Persistence.putDouble(
            this, Persistence.PREF_OLD_MAIN,
            Utils.toDouble(viewModel.prevMainMeter.value.orEmpty())
        )
        Persistence.putDouble(
            this, Persistence.PREF_OLD_GARDEN,
            Utils.toDouble(viewModel.prevGardenMeter.value.orEmpty())
        )
        Persistence.putDouble(
            this, Persistence.PREF_CURRENT_MAIN,
            Utils.toDouble(viewModel.currentMainMeter.value.orEmpty())
        )
        Persistence.putDouble(
            this, Persistence.PREF_CURRENT_GARDEN,
            Utils.toDouble(viewModel.currentGardenMeter.value.orEmpty())
        )
        super.onDestroy()
    }

    private fun loadMeterStates() {
        val lines = Persistence.getString(this, Persistence.PREF_EMPTY_ACTIONS, "")
            .split('\n')
            .filterNot { it.isEmpty() }
        if (lines.isEmpty()) {
            prevEmptyActions.clear()
        } else {
            prevEmptyActions = lines.map { MeterStates.fromString(it) }.toMutableList()
        }
    }

    private fun saveMeterStates() {
        val data = prevEmptyActions.joinToString(separator = "\n")
        Persistence.putString(this, Persistence.PREF_EMPTY_ACTIONS, data)
    }

    private fun refreshMeterStates() {
        val text = prevEmptyActions.joinToString(
            separator = "\n",
            transform = { it.toVisibleString() }
        )
        viewModel.prevEmptyActions.value =
            if (text.isNotEmpty()) text else getString(R.string.no_data)
    }

    private fun refreshCalculation() {
        val prevMain = Utils.toDouble(viewModel.prevMainMeter.value.orEmpty())
        val prevGarden = Utils.toDouble(viewModel.prevGardenMeter.value.orEmpty())
        val currentMain = Utils.toDouble(viewModel.currentMainMeter.value.orEmpty())
        val currentGarden = Utils.toDouble(viewModel.currentGardenMeter.value.orEmpty())
        val usage = currentMain - prevMain - (currentGarden - prevGarden)
        val usageText = String.format(Locale.GERMANY, "%1$.2f", usage)
        val percentage = (usage * 100.0 / FULL_CONTAINER).roundToInt()
        viewModel.waterUsage.value =
            Html.fromHtml(
                "$usageText m<sup><small>3</small></sup>  ($percentage%)",
                Html.FROM_HTML_MODE_LEGACY
            )

        if (prevEmptyActions.isNotEmpty() && percentage > 0) {
            val lastEmptyAction = prevEmptyActions.last()
            val hours = (System.currentTimeMillis() - lastEmptyAction.date) / MS_IN_HOUR
            val hoursTotal = hours * 100 / percentage
            val hoursLeft = hoursTotal - hours
            viewModel.daysLeft.value = Utils.toDaysHours(this, hoursLeft)
            binding.daysLeft.setTextColor(
                when {
                    hoursLeft < HOURS_WARN2 -> COLOR_WARN2
                    hoursLeft < HOURS_WARN1 -> COLOR_WARN1
                    else -> COLOR_NORMAL
                }
            )
        } else {
            viewModel.daysLeft.value = ""
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        refreshCalculation()
    }

    private fun emptyTankClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.empty_tank)
            .setMessage(R.string.empty_tank_question)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.prevMainMeter.value = viewModel.currentMainMeter.value
                viewModel.prevGardenMeter.value = viewModel.currentGardenMeter.value
                refreshCalculation()

                val meterStates = MeterStates(
                    date = System.currentTimeMillis(),
                    mainMeter = Utils.toDouble(viewModel.prevMainMeter.value.orEmpty()),
                    gardenMeter = Utils.toDouble(viewModel.prevGardenMeter.value.orEmpty())
                )
                prevEmptyActions.add(meterStates)
                saveMeterStates()
                refreshMeterStates()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }
}