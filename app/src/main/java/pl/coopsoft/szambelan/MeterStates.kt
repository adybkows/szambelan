package pl.coopsoft.szambelan

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class MeterStates(val date: Long, val mainMeter: Double, val gardenMeter: Double) {

    override fun toString(): String {
        return "$date;$mainMeter;$gardenMeter"
    }

    fun toVisibleString(context: Context, prev: MeterStates?): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date(date))
        val mainStr = Utils.toString(mainMeter)
        val gardenStr = Utils.toString(gardenMeter)
        val days = if (prev == null) "" else context.getString(R.string.days_fmt, daysSince(prev))
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