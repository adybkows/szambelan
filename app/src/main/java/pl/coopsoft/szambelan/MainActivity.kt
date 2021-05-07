package pl.coopsoft.szambelan

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import java.util.*
import kotlin.math.roundToInt

class MainActivity : Activity(), TextWatcher {

    private companion object {
        private const val PREFERENCES_NAME = "preferences"
        private const val PREF_OLD_MAIN = "old_main"
        private const val PREF_OLD_GARDEN = "old_garden"
        private const val PREF_CURRENT_MAIN = "current_main"
        private const val PREF_CURRENT_GARDEN = "current_garden"
        private const val FULL_CONTAINER = 6.0 // [m^3]
    }

    private lateinit var oldMainEditText: EditText
    private lateinit var oldGardenEditText: EditText
    private lateinit var currentMainEditText: EditText
    private lateinit var currentGardenEditText: EditText
    private lateinit var waterUsageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        oldMainEditText = findViewById(R.id.oldMainEditText)
        oldGardenEditText = findViewById(R.id.oldGardenEditText)
        currentMainEditText = findViewById(R.id.currentMainEditText)
        currentGardenEditText = findViewById(R.id.currentGardenEditText)
        waterUsageText = findViewById(R.id.waterUsage)

        oldMainEditText.setText(getPref(this, PREF_OLD_MAIN))
        oldGardenEditText.setText(getPref(this, PREF_OLD_GARDEN))
        currentMainEditText.setText(getPref(this, PREF_CURRENT_MAIN))
        currentGardenEditText.setText(getPref(this, PREF_CURRENT_GARDEN))

        refreshCalculation()

        oldMainEditText.addTextChangedListener(this)
        oldGardenEditText.addTextChangedListener(this)
        currentMainEditText.addTextChangedListener(this)
        currentGardenEditText.addTextChangedListener(this)
    }

    override fun onDestroy() {
        putPref(this, PREF_OLD_MAIN, oldMainEditText.text.toString())
        putPref(this, PREF_OLD_GARDEN, oldGardenEditText.text.toString())
        putPref(this, PREF_CURRENT_MAIN, currentMainEditText.text.toString())
        putPref(this, PREF_CURRENT_GARDEN, currentGardenEditText.text.toString())
        super.onDestroy()
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
                "$usageText m<sup><small>3</small></sup>  ($percentage%)", Html.FROM_HTML_MODE_LEGACY
            )
    }

    private fun getNumber(editText: EditText) =
        editText.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0

    private fun getPref(context: Context, key: String) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(key, "0,0")

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
}