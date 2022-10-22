package pl.coopsoft.szambelan.domain.model

import android.content.Context
import androidx.annotation.Keep
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
data class MeterStates(val date: Long, val mainMeter: Double, val gardenMeter: Double) {

    constructor() : this(0L, 0.0, 0.0)

    override fun toString(): String {
        return "$date;$mainMeter;$gardenMeter"
    }

    fun toVisibleString(
        context: Context,
        formattingUtils: FormattingUtils,
        prev: MeterStates?
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date(date))
        val mainStr = formattingUtils.toString(mainMeter)
        val gardenStr = formattingUtils.toString(gardenMeter)
        val days =
            if (prev == null)
                ""
            else {
                val daysSince = daysSince(prev).toInt()
                "  ${context.resources.getQuantityString(R.plurals.days_fmt, daysSince, daysSince)}"
            }
        return "$dateStr  $mainStr  $gardenStr$days"
    }

    private fun daysSince(prev: MeterStates): Long {
        val ms = date - prev.date
        return ms / MS_IN_DAY
    }

    companion object {
        private const val MS_IN_HOUR = 3600000L
        private const val MS_IN_DAY = MS_IN_HOUR * 24L

        fun fromString(data: String): MeterStates {
            val parts = data.split(';')
            return MeterStates(
                date = parts[0].toLong(),
                mainMeter = parts[1].toDouble(),
                gardenMeter = parts[2].toDouble()
            )
        }
    }
}