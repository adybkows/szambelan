package pl.coopsoft.szambelan

import java.text.NumberFormat
import java.text.ParseException
import java.util.*

object Utils {
    fun toDouble(s: String) =
        try {
            NumberFormat.getInstance().parse(s)?.toDouble() ?: 0.0
        } catch (e: ParseException) {
            0.0
        }

    fun toString(d: Double) =
        String.format(Locale.getDefault(), "%1$.2f", d)
}