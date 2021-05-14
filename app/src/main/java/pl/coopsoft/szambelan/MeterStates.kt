package pl.coopsoft.szambelan

import java.text.SimpleDateFormat
import java.util.*

class MeterStates(val date: Long, val mainMeter: Double, val gardenMeter: Double) {

    override fun toString(): String {
        return "$date;$mainMeter;$gardenMeter".replace(',', '.')
    }

    fun toVisibleString() =
        "${DATE_FORMAT.format(Date(date))}  $mainMeter  $gardenMeter".replace('.', ',')

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

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