package pl.coopsoft.szambelan.utils

import android.content.Context
import pl.coopsoft.szambelan.R
import java.util.*

object FormattingUtils {
    fun toDouble(s: String) =
        s.replace(',', '.').toDoubleOrNull() ?: 0.0

    fun toString(d: Double) =
        String.format(Locale.getDefault(), "%1$.2f", d)

    fun toDaysHours(context: Context, hours: Long): String {
        val dd = context.getString(R.string.dd)
        val hh = context.getString(R.string.hh)
        val days = hours / 24
        val hoursLeft = hours % 24
        return when {
            days == 0L -> "$hours$hh"
            hoursLeft == 0L -> "$days$dd"
            else -> "$days$dd $hoursLeft$hh"
        }
    }

    fun isDigitOrDot(c: Char): Boolean =
        (c in '0'..'9') || (c == '.') || (c == ',')

    fun maxOneDot(s: String): String {
        var dot = false
        return s.filter {
            if (it != '.' && it != ',')
                true
            else if (!dot) {
                dot = true
                true
            } else
                false
        }
    }
}