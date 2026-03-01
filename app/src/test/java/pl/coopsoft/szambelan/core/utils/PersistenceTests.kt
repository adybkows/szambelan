package pl.coopsoft.szambelan.core.utils

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PersistenceTests {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var persistence: Persistence

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test.preferences_pb") }
        )
        persistence = Persistence(dataStore)
    }

    @Test
    fun testSetAndGetThemeMode() = runTest(testDispatcher) {
        assertThat(persistence.themeModeFlow.first()).isEqualTo(0)
        persistence.setThemeMode(2)
        assertThat(persistence.themeModeFlow.first()).isEqualTo(2)
    }

    @Test
    fun testSetAndGetUserEmail() = runTest(testDispatcher) {
        assertThat(persistence.userEmailFlow.first()).isEqualTo("")
        persistence.setUserEmail("test@example.com")
        assertThat(persistence.userEmailFlow.first()).isEqualTo("test@example.com")
    }

    @Test
    fun testSaveAndGetMeterData() = runTest(testDispatcher) {
        val initialData = persistence.getMeterData()
        assertThat(initialData.currentMain).isWithin(1e-6).of(0.0)

        persistence.saveMeterData(
            currentGarden = 1.1,
            currentMain = 2.2,
            oldGarden = 3.3,
            oldMain = 4.4
        )

        val savedData = persistence.getMeterData()
        assertThat(savedData.currentGarden).isWithin(1e-6).of(1.1)
        assertThat(savedData.currentMain).isWithin(1e-6).of(2.2)
        assertThat(savedData.oldGarden).isWithin(1e-6).of(3.3)
        assertThat(savedData.oldMain).isWithin(1e-6).of(4.4)
    }

    @Test
    fun testSaveAndGetEmptyActions() = runTest(testDispatcher) {
        persistence.saveEmptyActions("action1\naction2")
        val data = persistence.getMeterData()
        assertThat(data.emptyActions).isEqualTo("action1\naction2")
    }
}
