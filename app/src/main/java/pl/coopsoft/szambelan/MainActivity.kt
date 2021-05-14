package pl.coopsoft.szambelan

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.*
import kotlin.math.roundToInt

class MainActivity : Activity(), TextWatcher {

    private companion object {
        private const val PREFERENCES_NAME = "preferences"
        private const val PREF_EMPTY_ACTIONS = "empty_actions"
        private const val PREF_OLD_MAIN = "old_main"
        private const val PREF_OLD_GARDEN = "old_garden"
        private const val PREF_CURRENT_MAIN = "current_main"
        private const val PREF_CURRENT_GARDEN = "current_garden"
        private const val FULL_CONTAINER = 6.0 // [m^3]
    }

    private lateinit var prevEmptyActionsTextView: TextView
    private lateinit var oldMainEditText: EditText
    private lateinit var oldGardenEditText: EditText
    private lateinit var currentMainEditText: EditText
    private lateinit var currentGardenEditText: EditText
    private lateinit var waterUsageText: TextView

    var prevEmptyActions = mutableListOf<MeterStates>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prevEmptyActionsTextView = findViewById(R.id.prevEmptyActions)
        oldMainEditText = findViewById(R.id.oldMainEditText)
        oldGardenEditText = findViewById(R.id.oldGardenEditText)
        currentMainEditText = findViewById(R.id.currentMainEditText)
        currentGardenEditText = findViewById(R.id.currentGardenEditText)
        waterUsageText = findViewById(R.id.waterUsage)

        oldMainEditText.setText(getPref(this, PREF_OLD_MAIN))
        oldGardenEditText.setText(getPref(this, PREF_OLD_GARDEN))
        currentMainEditText.setText(getPref(this, PREF_CURRENT_MAIN))
        currentGardenEditText.setText(getPref(this, PREF_CURRENT_GARDEN))

        loadMeterStates()
        refreshMeterStates()
        refreshCalculation()

        oldMainEditText.addTextChangedListener(this)
        oldGardenEditText.addTextChangedListener(this)
        currentMainEditText.addTextChangedListener(this)
        currentGardenEditText.addTextChangedListener(this)

        findViewById<Button>(R.id.emptyTankButton).setOnClickListener { emptyTankClicked() }
    }

    override fun onDestroy() {
        putPref(this, PREF_OLD_MAIN, oldMainEditText.text.toString())
        putPref(this, PREF_OLD_GARDEN, oldGardenEditText.text.toString())
        putPref(this, PREF_CURRENT_MAIN, currentMainEditText.text.toString())
        putPref(this, PREF_CURRENT_GARDEN, currentGardenEditText.text.toString())
        super.onDestroy()
    }

    private fun loadMeterStates() {
        val lines = getPref(this, PREF_EMPTY_ACTIONS, "")
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
        putPref(this, PREF_EMPTY_ACTIONS, data)
    }

    private fun refreshMeterStates() {
        val text = prevEmptyActions.joinToString(
            separator = "\n",
            transform = { it.toVisibleString() }
        )
        prevEmptyActionsTextView.text = if (text.isNotEmpty()) text else getString(R.string.no_data)
    }

    private fun refreshCalculation() {
        val oldMain = getNumber(oldMainEditText)
        val oldGarden = getNumber(oldGardenEditText)
        val currentMain = getNumber(currentMainEditText)
        val currentGarden = getNumber(currentGardenEditText)
        val usage = currentMain - oldMain - (currentGarden - oldGarden)
        val usageText = String.format(Locale.GERMANY, "%1$.2f", usage)
        val percentage = (usage * 100.0 / FULL_CONTAINER).roundToInt()
        waterUsageText.text =
            Html.fromHtml(
                "$usageText m<sup><small>3</small></sup>  ($percentage%)",
                Html.FROM_HTML_MODE_LEGACY
            )
    }

    private fun getNumber(editText: EditText) =
        editText.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

    private fun getPref(context: Context, key: String, defValue: String = "0,0") =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(key, defValue).orEmpty()

    private fun putPref(context: Context, key: String, value: String) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().putString(key, value).apply()

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
                oldMainEditText.text = currentMainEditText.text
                oldGardenEditText.text = currentGardenEditText.text
                refreshCalculation()

                val meterStates = MeterStates(
                    date = System.currentTimeMillis(),
                    mainMeter = getNumber(oldMainEditText),
                    gardenMeter = getNumber(oldGardenEditText)
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