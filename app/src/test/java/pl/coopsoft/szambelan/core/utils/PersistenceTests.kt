package pl.coopsoft.szambelan.core.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTests {

    private val formattingUtils = FormattingUtils()
    private val persistence = Persistence(formattingUtils)

    @Test
    fun testPutAndGetString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(persistence.getString(context, "key")).isEqualTo("0.00")
        persistence.putString(context, "key1", "value1")
        assertThat(persistence.getString(context, "key1")).isEqualTo("value1")
    }

    @Test
    fun testPutAndGetDouble() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(persistence.getDouble(context, "key")).isEqualTo(0.0)
        persistence.putDouble(context, "key2", 123.456)
        assertThat(persistence.getDouble(context, "key2")).isWithin(0.00001).of(123.456)
    }
}