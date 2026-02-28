package pl.coopsoft.szambelan.core.utils

import android.os.Build
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import pl.coopsoft.testutils.EmptyTestActivity

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class GoogleSignInLauncherTests {

    private companion object {
        private const val TEST_GCP_ID = "abcd"
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EmptyTestActivity>()

    @Test
    fun testGoogleSignInLauncher() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        val onSignedIn = mockk<(Boolean) -> Unit>(relaxed = true)
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onFailure = mockk<() -> Unit>(relaxed = true)
        val auth = Firebase.auth
        val googleSignInHelper = GoogleSignInHelper(auth)
        val googleSignInClient =
            spyk(googleSignInHelper.googleSignInClient(composeTestRule.activity))

        composeTestRule.activity.setContent {
            val googleSignInLauncher =
                googleSignInHelper.googleSignInLauncher(googleSignInClient, onSignedIn)

            Button(onClick = {
                googleSignInHelper.googleSignIn(
                    composeTestRule.activity, googleSignInClient, googleSignInLauncher, TEST_GCP_ID,
                    onSuccess, onFailure
                )
            }) {
                Text("OK")
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").performClick()

        verify { googleSignInClient.beginSignIn(any()) }

        Robolectric.flushForegroundThreadScheduler()
        Thread.sleep(100)
        Robolectric.flushForegroundThreadScheduler()

        verify { onFailure.invoke() }
        verify(exactly = 0) { onSuccess.invoke() }
        verify(exactly = 0) { onSignedIn.invoke(any()) }
    }
}
