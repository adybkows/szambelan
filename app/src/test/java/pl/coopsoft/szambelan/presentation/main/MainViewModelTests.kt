package pl.coopsoft.szambelan.presentation.main

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.same
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import pl.coopsoft.szambelan.core.utils.Persistence
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.model.MeterStates
import pl.coopsoft.szambelan.domain.usecase.login.LogOutUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.DownloadUseCase
import pl.coopsoft.szambelan.domain.usecase.transfer.UploadUseCase

@RunWith(AndroidJUnit4::class)
class MainViewModelTests {

    private companion object {
        private const val CURRENT_MAIN = 1240.0
        private const val CURRENT_GARDEN = 346.0
        private const val OLD_MAIN = 1234.54
        private const val OLD_GARDEN = 345.67
        private const val CURRENT_MAIN_STR = "1240.00"
        private const val CURRENT_GARDEN_STR = "346.00"
        private const val OLD_MAIN_STR = "1234.54"
        private const val OLD_GARDEN_STR = "345.67"
        private const val EMPTY_ACTIONS_STR =
            "1621006842873;811.1;5.332\n1622032134537;820.6;9.67\n1623325662207;834.53;18.07"
        private val EMPTY_ACTIONS = mutableListOf(
            MeterStates(1621006842873, 811.1, 5.332),
            MeterStates(1622032134537, 820.6, 9.67),
            MeterStates(1623325662207, 834.53, 18.07)
        )
    }

    private val downloadUseCase = mock<DownloadUseCase>()
    private val logOutUseCase = mock<LogOutUseCase>()
    private val persistence = mock<Persistence>()
    private val uploadUseCase = mock<UploadUseCase>()
    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        reset(downloadUseCase)
        reset(logOutUseCase)
        reset(persistence)
        reset(uploadUseCase)
        val application = ApplicationProvider.getApplicationContext<Application>()
        mainViewModel = spy(
            MainViewModel(
                application, downloadUseCase, logOutUseCase, persistence, uploadUseCase,
                FormattingUtils()
            )
        )
    }

    @Test
    fun testLoadSavedData() {
        mainViewModel.loadSavedData()

        verify(mainViewModel).loadEditValues()
        verify(mainViewModel).loadMeterStates()
        verify(mainViewModel).showMeterStates()
        verify(mainViewModel).refreshCalculation()
    }

    @Test
    fun testLoadEditValues() {
        whenever(persistence.getDouble(any(), eq(Persistence.PREF_OLD_MAIN), any()))
            .thenReturn(OLD_MAIN)
        whenever(persistence.getDouble(any(), eq(Persistence.PREF_OLD_GARDEN), any()))
            .thenReturn(OLD_GARDEN)
        whenever(persistence.getDouble(any(), eq(Persistence.PREF_CURRENT_MAIN), any()))
            .thenReturn(CURRENT_MAIN)
        whenever(persistence.getDouble(any(), eq(Persistence.PREF_CURRENT_GARDEN), any()))
            .thenReturn(CURRENT_GARDEN)

        assertThat(mainViewModel.prevMainMeter.value).isEmpty()
        assertThat(mainViewModel.prevGardenMeter.value).isEmpty()
        assertThat(mainViewModel.currentMainMeter.value).isEmpty()
        assertThat(mainViewModel.currentGardenMeter.value).isEmpty()

        mainViewModel.loadEditValues()

        assertThat(mainViewModel.prevMainMeter.value).isEqualTo(OLD_MAIN_STR)
        assertThat(mainViewModel.prevGardenMeter.value).isEqualTo(OLD_GARDEN_STR)
        assertThat(mainViewModel.currentMainMeter.value).isEqualTo(CURRENT_MAIN_STR)
        assertThat(mainViewModel.currentGardenMeter.value).isEqualTo(CURRENT_GARDEN_STR)
    }

    @Test
    fun testLoadMeterStates() {
        whenever(persistence.getString(any(), eq(Persistence.PREF_EMPTY_ACTIONS), any()))
            .thenReturn(EMPTY_ACTIONS_STR)

        assertThat(mainViewModel.emptyActions).isEmpty()

        mainViewModel.loadMeterStates()

        assertThat(mainViewModel.emptyActions).isEqualTo(EMPTY_ACTIONS)
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
        mainViewModel.prevMainMeter.value = OLD_MAIN_STR
        mainViewModel.prevGardenMeter.value = OLD_GARDEN_STR
        mainViewModel.currentMainMeter.value = CURRENT_MAIN_STR
        mainViewModel.currentGardenMeter.value = CURRENT_GARDEN_STR
        mainViewModel.emptyActions.clear()

        mainViewModel.refreshCalculation()

        assertThat(mainViewModel.waterUsage.value.toString()).isEqualTo("5.13 m3  (86%)")
        assertThat(mainViewModel.daysLeft.value).isEmpty()

        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 3600 * 1000
        mainViewModel.emptyActions.add(MeterStates(weekAgo, OLD_MAIN, OLD_GARDEN))

        mainViewModel.refreshCalculation()

        assertThat(mainViewModel.waterUsage.value.toString()).isEqualTo("5.13 m3  (86%)")
        assertThat(mainViewModel.daysSince.value).isEqualTo("7d")
        assertThat(mainViewModel.daysLeft.value).isEqualTo("1d 3h")
        assertThat(mainViewModel.daysLeftColor.value).isEqualTo(Color.Red)
    }

    @Test
    fun testDownloadClicked() {
        assertThat(mainViewModel.prevMainMeter.value).isEmpty()
        assertThat(mainViewModel.prevGardenMeter.value).isEmpty()
        assertThat(mainViewModel.currentMainMeter.value).isEmpty()
        assertThat(mainViewModel.currentGardenMeter.value).isEmpty()

        val data = DataModel(
            prevMainMeter = OLD_MAIN, prevGardenMeter = OLD_GARDEN,
            currentMainMeter = CURRENT_MAIN, currentGardenMeter = CURRENT_GARDEN,
            emptyActions = EMPTY_ACTIONS
        )
        val callbackCaptor = argumentCaptor<(DataModel) -> Unit>()

        mainViewModel.downloadClicked()

        verify(downloadUseCase).askAndDownload(same(mainViewModel), callbackCaptor.capture())
        callbackCaptor.firstValue.invoke(data)

        assertThat(mainViewModel.prevMainMeter.value).isEqualTo(OLD_MAIN_STR)
        assertThat(mainViewModel.prevGardenMeter.value).isEqualTo(OLD_GARDEN_STR)
        assertThat(mainViewModel.currentMainMeter.value).isEqualTo(CURRENT_MAIN_STR)
        assertThat(mainViewModel.currentGardenMeter.value).isEqualTo(CURRENT_GARDEN_STR)
        assertThat(mainViewModel.emptyActions).isEqualTo(data.emptyActions)

        verify(mainViewModel).showMeterStates()
        verify(mainViewModel).refreshCalculation()
        verify(mainViewModel).saveEditValues()
        verify(mainViewModel).saveMeterStates()
    }

    @Test
    fun testUploadClicked() {
        val data = DataModel(
            prevMainMeter = OLD_MAIN, prevGardenMeter = OLD_GARDEN,
            currentMainMeter = CURRENT_MAIN, currentGardenMeter = CURRENT_GARDEN,
            emptyActions = EMPTY_ACTIONS
        )

        mainViewModel.prevMainMeter.value = OLD_MAIN_STR
        mainViewModel.prevGardenMeter.value = OLD_GARDEN_STR
        mainViewModel.currentMainMeter.value = CURRENT_MAIN_STR
        mainViewModel.currentGardenMeter.value = CURRENT_GARDEN_STR
        mainViewModel.emptyActions = EMPTY_ACTIONS

        mainViewModel.uploadClicked()

        verify(uploadUseCase).askAndUpload(same(mainViewModel), eq(data))
    }

    @Test
    fun testSaveEditValues() {
        mainViewModel.prevMainMeter.value = OLD_MAIN_STR
        mainViewModel.prevGardenMeter.value = OLD_GARDEN_STR
        mainViewModel.currentMainMeter.value = CURRENT_MAIN_STR
        mainViewModel.currentGardenMeter.value = CURRENT_GARDEN_STR

        mainViewModel.saveEditValues()

        verify(persistence).putDouble(any(), eq(Persistence.PREF_OLD_MAIN), eq(OLD_MAIN))
        verify(persistence).putDouble(any(), eq(Persistence.PREF_OLD_GARDEN), eq(OLD_GARDEN))
        verify(persistence).putDouble(any(), eq(Persistence.PREF_CURRENT_MAIN), eq(CURRENT_MAIN))
        verify(persistence)
            .putDouble(any(), eq(Persistence.PREF_CURRENT_GARDEN), eq(CURRENT_GARDEN))
    }

    @Test
    fun testUpdateDecimalSeparator() {
        assertThat(mainViewModel.updateDecimalSeparator("123.456")).isEqualTo("123.456")
        assertThat(mainViewModel.updateDecimalSeparator("123,456")).isEqualTo("123.456")
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