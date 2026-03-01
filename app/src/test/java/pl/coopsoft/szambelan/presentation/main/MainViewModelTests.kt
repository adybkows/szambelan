package pl.coopsoft.szambelan.presentation.main

import android.app.Application
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import pl.coopsoft.szambelan.core.utils.Persistence
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.model.MeterData
import pl.coopsoft.szambelan.domain.model.MeterStates
import pl.coopsoft.szambelan.domain.usecase.login.LogOutUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.DownloadUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.UploadUseCase
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class MainViewModelTests {

    private companion object {
        private const val CURRENT_MAIN = 1240.0
        private const val CURRENT_GARDEN = 346.0
        private const val OLD_MAIN = 1234.54
        private const val OLD_GARDEN = 345.67
        private const val EMPTY_ACTIONS_STR =
            "1621006842873;811.1;5.332\n1622032134537;820.6;9.67\n1623325662207;834.53;18.07"
        private val EMPTY_ACTIONS = mutableListOf(
            MeterStates(1621006842873, 811.1, 5.332),
            MeterStates(1622032134537, 820.6, 9.67),
            MeterStates(1623325662207, 834.53, 18.07)
        )
    }

    private val testDispatcher = StandardTestDispatcher()
    private val downloadUseCase = mockk<DownloadUseCase>(relaxed = true)
    private val logOutUseCase = mockk<LogOutUseCase>(relaxed = true)
    private val persistence = mockk<Persistence>(relaxed = true)
    private val uploadUseCase = mockk<UploadUseCase>(relaxed = true)
    private lateinit var mainViewModel: MainViewModel
    private val formattingUtils = FormattingUtils()

    private val currentMainStr get() = formattingUtils.toString(CURRENT_MAIN)
    private val currentGardenStr get() = formattingUtils.toString(CURRENT_GARDEN)
    private val oldMainStr get() = formattingUtils.toString(OLD_MAIN)
    private val oldGardenStr get() = formattingUtils.toString(OLD_GARDEN)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)
        every { persistence.themeModeFlow } returns flowOf(0)
        val application = ApplicationProvider.getApplicationContext<Application>()
        mainViewModel = spyk(
            MainViewModel(
                application, downloadUseCase, logOutUseCase, persistence, uploadUseCase,
                formattingUtils
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadSavedData() = runTest {
        val meterData = MeterData(
            currentGarden = CURRENT_GARDEN,
            currentMain = CURRENT_MAIN,
            oldGarden = OLD_GARDEN,
            oldMain = OLD_MAIN,
            emptyActions = EMPTY_ACTIONS_STR
        )
        coEvery { persistence.getMeterData() } returns meterData

        mainViewModel.loadSavedData()
        advanceUntilIdle()

        assertThat(mainViewModel.prevMainMeter.value).isEqualTo(oldMainStr)
        assertThat(mainViewModel.prevGardenMeter.value).isEqualTo(oldGardenStr)
        assertThat(mainViewModel.currentMainMeter.value).isEqualTo(currentMainStr)
        assertThat(mainViewModel.currentGardenMeter.value).isEqualTo(currentGardenStr)
        assertThat(mainViewModel.emptyActions).isEqualTo(EMPTY_ACTIONS)

        verify { mainViewModel["showMeterStates"]() }
        verify { mainViewModel.refreshCalculation() }
    }

    @Test
    fun testShowMeterStates() {
        mainViewModel.showMeterStates()
        assertThat(mainViewModel.prevEmptyActions.value).isEmpty()

        mainViewModel.emptyActions = EMPTY_ACTIONS

        mainViewModel.showMeterStates()
        assertThat(mainViewModel.prevEmptyActions.value).isEqualTo(
            listOf(
                "2021-05-14  811.10  5.33",
                "2021-05-26  820.60  9.67  (11 days)",
                "2021-06-10  834.53  18.07  (14 days)"
            )
        )
    }

    @Test
    fun testRefreshCalculation() {
        mainViewModel.prevMainMeter.value = oldMainStr
        mainViewModel.prevGardenMeter.value = oldGardenStr
        mainViewModel.currentMainMeter.value = currentMainStr
        mainViewModel.currentGardenMeter.value = currentGardenStr
        mainViewModel.emptyActions.clear()

        mainViewModel.refreshCalculation()

        val usageText = formattingUtils.toString(5.13)
        assertThat(mainViewModel.waterUsage.value.toString()).isEqualTo("$usageText m3  (86%)")
        assertThat(mainViewModel.daysLeft.value).isEmpty()

        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 3600 * 1000
        mainViewModel.emptyActions.add(MeterStates(weekAgo, OLD_MAIN, OLD_GARDEN))

        mainViewModel.refreshCalculation()

        assertThat(mainViewModel.waterUsage.value.toString()).isEqualTo("$usageText m3  (86%)")
        assertThat(mainViewModel.daysSince.value).isEqualTo("7d")
        assertThat(mainViewModel.daysLeft.value).isEqualTo("1d 3h")
        assertThat(mainViewModel.daysLeftColor.value).isEqualTo(Color.Red)
    }

    @Test
    fun testDownloadClicked() = runTest {
        assertThat(mainViewModel.prevMainMeter.value).isEmpty()
        assertThat(mainViewModel.prevGardenMeter.value).isEmpty()
        assertThat(mainViewModel.currentMainMeter.value).isEmpty()
        assertThat(mainViewModel.currentGardenMeter.value).isEmpty()

        val data = DataModel(
            prevMainMeter = OLD_MAIN, prevGardenMeter = OLD_GARDEN,
            currentMainMeter = CURRENT_MAIN, currentGardenMeter = CURRENT_GARDEN,
            emptyActions = EMPTY_ACTIONS
        )
        val callbackSlot = slot<(DataModel) -> Unit>()

        mainViewModel.downloadClicked()

        verify { downloadUseCase.askAndDownload(any(), capture(callbackSlot)) }
        callbackSlot.captured.invoke(data)
        advanceUntilIdle()

        assertThat(mainViewModel.prevMainMeter.value).isEqualTo(oldMainStr)
        assertThat(mainViewModel.prevGardenMeter.value).isEqualTo(oldGardenStr)
        assertThat(mainViewModel.currentMainMeter.value).isEqualTo(currentMainStr)
        assertThat(mainViewModel.currentGardenMeter.value).isEqualTo(currentGardenStr)
        assertThat(mainViewModel.emptyActions).isEqualTo(data.emptyActions)

        verify { mainViewModel.showMeterStates() }
        verify { mainViewModel.refreshCalculation() }
        verify { mainViewModel.saveEditValues() }
        verify { mainViewModel["saveMeterStates"]() }
    }

    @Test
    fun testUploadClicked() {
        val data = DataModel(
            prevMainMeter = OLD_MAIN, prevGardenMeter = OLD_GARDEN,
            currentMainMeter = CURRENT_MAIN, currentGardenMeter = CURRENT_GARDEN,
            emptyActions = EMPTY_ACTIONS
        )

        mainViewModel.prevMainMeter.value = oldMainStr
        mainViewModel.prevGardenMeter.value = oldGardenStr
        mainViewModel.currentMainMeter.value = currentMainStr
        mainViewModel.currentGardenMeter.value = currentGardenStr
        mainViewModel.emptyActions = EMPTY_ACTIONS

        mainViewModel.uploadClicked()

        verify { uploadUseCase.askAndUpload(any(), eq(data)) }
    }

    @Test
    fun testSaveEditValues() = runTest {
        mainViewModel.prevMainMeter.value = oldMainStr
        mainViewModel.prevGardenMeter.value = oldGardenStr
        mainViewModel.currentMainMeter.value = currentMainStr
        mainViewModel.currentGardenMeter.value = currentGardenStr

        mainViewModel.saveEditValues()
        advanceUntilIdle()

        coVerify {
            persistence.saveMeterData(
                currentGarden = CURRENT_GARDEN,
                currentMain = CURRENT_MAIN,
                oldGarden = OLD_GARDEN,
                oldMain = OLD_MAIN
            )
        }
    }

    @Test
    fun testUpdateDecimalSeparator() {
        val separator = java.text.DecimalFormatSymbols.getInstance().decimalSeparator
        val other = if (separator == '.') ',' else '.'

        assertThat(mainViewModel.updateDecimalSeparator("123${separator}456")).isEqualTo("123${separator}456")
        assertThat(mainViewModel.updateDecimalSeparator("123${other}456")).isEqualTo("123${separator}456")
    }

    @Test
    fun testLimitLines() {
        assertThat(mainViewModel.limitLines(EMPTY_ACTIONS_STR, 0)).isEmpty()
        assertThat(mainViewModel.limitLines(EMPTY_ACTIONS_STR, 1)).isEqualTo(
            "1623325662207;834.53;18.07"
        )
        assertThat(mainViewModel.limitLines(EMPTY_ACTIONS_STR, 2)).isEqualTo(
            "1622032134537;820.6;9.67\n1623325662207;834.53;18.07"
        )
        assertThat(mainViewModel.limitLines(EMPTY_ACTIONS_STR, 3)).isEqualTo(EMPTY_ACTIONS_STR)
    }

}
