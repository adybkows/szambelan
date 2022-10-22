package pl.coopsoft.szambelan.domain.repository.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog
import pl.coopsoft.szambelan.core.di.Providers
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.model.MeterStates

@RunWith(AndroidJUnit4::class)
class RemoteStorageHelperTests {

    private companion object {
        private const val TEST_DATE = 1640995200000
        private const val TEST_USER = "user1"
        private const val TEST_JSON = "{\"prevMainMeter\":10,\"prevGardenMeter\":5," +
                "\"currentMainMeter\":12,\"currentGardenMeter\":6," +
                "\"emptyActions\":[{\"date\":1640995200000,\"mainMeter\":10,\"gardenMeter\":5}]}"
        private val TEST_DATA = DataModel(
            prevMainMeter = 10.0, prevGardenMeter = 5.0,
            currentMainMeter = 12.0, currentGardenMeter = 6.0,
            emptyActions = listOf(
                MeterStates(date = TEST_DATE, mainMeter = 10.0, gardenMeter = 5.0)
            )
        )
    }

    private lateinit var server: MockWebServer
    private lateinit var service: RemoteStorageService
    private lateinit var remoteStorageHelper: RemoteStorageHelper

    @Before
    fun setup() {
        ShadowLog.stream = System.out
        server = MockWebServer()
        server.start()
        service = Providers().provideRemoteStorageService(server.url("/").toString())
        remoteStorageHelper = RemoteStorageHelper(service)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun testDownloadDataSuccess() {
        server.enqueue(MockResponse().setBody(TEST_JSON))

        val done = mockk<(DataModel?) -> Unit> {
            justRun { this@mockk.invoke(any<DataModel>()) }
        }
        remoteStorageHelper.downloadData(TEST_USER, done)

        verify(timeout = 1000) { done.invoke(TEST_DATA) }

        val request = server.takeRequest()
        assertThat(request.method).isEqualTo("GET")
        assertThat(request.path).isEqualTo("/data_user1.xml")
    }

    @Test
    fun testDownloadDataFailure() {
        server.enqueue(MockResponse().setResponseCode(404))

        val done = mockk<(DataModel?) -> Unit> {
            justRun { this@mockk.invoke(null) }
        }
        remoteStorageHelper.downloadData(TEST_USER, done)

        verify(timeout = 1000) { done.invoke(null) }
    }

    @Test
    fun testUploadData() {
        server.enqueue(MockResponse().setResponseCode(200))

        val done = mockk<(Throwable?) -> Unit> {
            justRun { this@mockk.invoke(null) }
        }
        remoteStorageHelper.uploadData(TEST_DATA, TEST_USER, done)

        verify(timeout = 1000) { done.invoke(null) }

        val request = server.takeRequest()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.path).isEqualTo("/upload.php")
    }
}