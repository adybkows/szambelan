package pl.coopsoft.szambelan

import java.text.SimpleDateFormat
import java.util.*

class MeterStates(val date: Long, val mainMeter: Double, val gardenMeter: Double) {

    override fun toString(): String {
        return "$date;$mainMeter;$gardenMeter"
    }

    fun toVisibleString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date(date))
        val mainStr = Utils.toString(mainMeter)
        val gardenStr = Utils.toString(gardenMeter)
        return "$dateStr  $mainStr  $gardenStr"
    }

    companion object {
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