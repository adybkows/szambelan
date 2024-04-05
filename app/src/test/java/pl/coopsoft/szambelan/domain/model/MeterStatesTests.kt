package pl.coopsoft.szambelan.domain.model

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pl.coopsoft.szambelan.core.utils.FormattingUtils

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class MeterStatesTests {

    private companion object {
        private const val TEST_DATE_1 = 1640995200000
        private const val TEST_DATE_2 = 1639526400000
        private const val TEST_METER_1 = 1234.5432
        private const val TEST_METER_2 = 2222.2222
        private const val TEST_STRING = "1640995200000;1234.5432;2222.2222"
    }

    private val data1 =
        MeterStates(date = TEST_DATE_1, mainMeter = TEST_METER_1, gardenMeter = TEST_METER_2)

    private val data2 =
        MeterStates(date = TEST_DATE_2, mainMeter = TEST_METER_1, gardenMeter = TEST_METER_2)

    @Test
    fun testToString() {
        assertThat(data1.toString()).isEqualTo(TEST_STRING)
    }

    @Test
    fun testToVisibleString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val formattingUtils = FormattingUtils()
        assertThat(data1.toVisibleString(context, formattingUtils, null))
            .isEqualTo("2022-01-01  1234.54  2222.22")
        assertThat(data1.toVisibleString(context, formattingUtils, data2))
            .isEqualTo("2022-01-01  1234.54  2222.22  (17 days)")
    }

    @Test
    fun testFromString() {
        assertThat(MeterStates.fromString(TEST_STRING)).isEqualTo(data1)
    }
}