package pl.coopsoft.szambelan.core.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class FormattingUtilsTests {

    private val formattingUtils = FormattingUtils()

    @Test
    fun testToDouble() {
        assertThat(formattingUtils.toDouble("")).isEqualTo(0.0)
        assertThat(formattingUtils.toDouble("123.456")).isWithin(0.0000001).of(123.456)
        assertThat(formattingUtils.toDouble("-123,456")).isWithin(0.0000001).of(-123.456)
    }

    @Test
    fun testToString() {
        assertThat(formattingUtils.toString(0.0)).isEqualTo("0.00")
        assertThat(formattingUtils.toString(-123.0)).isEqualTo("-123.00")
        assertThat(formattingUtils.toString(4567.8901)).isEqualTo("4567.89")
    }

    @Test
    @Config(qualifiers = "fr")
    fun testToStringWithLocaleFR() {
        assertThat(formattingUtils.toString(0.0)).isEqualTo("0,00")
        assertThat(formattingUtils.toString(-123.0)).isEqualTo("-123,00")
        assertThat(formattingUtils.toString(4567.8901)).isEqualTo("4567,89")
    }

    @Test
    fun testToDaysHours() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(formattingUtils.toDaysHours(context, 0)).isEqualTo("0h")
        assertThat(formattingUtils.toDaysHours(context, 23)).isEqualTo("23h")
        assertThat(formattingUtils.toDaysHours(context, 24)).isEqualTo("1d")
        assertThat(formattingUtils.toDaysHours(context, 25)).isEqualTo("1d 1h")
        assertThat(formattingUtils.toDaysHours(context, 999)).isEqualTo("41d 15h")
    }

    @Test
    fun testIsDigitOrDot() {
        val chars =
            " 0123456789,./abcdefghijklmnopqrstuvwxyz"
        val results =
            "0111111111111000000000000000000000000000".map { it == '1' }
        for (index in chars.indices) {
            assertThat(formattingUtils.isDigitOrDot(chars[index])).isEqualTo(results[index])
        }
    }

    @Test
    fun testMaxOneDot() {
        assertThat(formattingUtils.maxOneDot("")).isEqualTo("")
        assertThat(formattingUtils.maxOneDot("12")).isEqualTo("12")
        assertThat(formattingUtils.maxOneDot("12.34")).isEqualTo("12.34")
        assertThat(formattingUtils.maxOneDot("12.34.56")).isEqualTo("12.3456")
    }
}