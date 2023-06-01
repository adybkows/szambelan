package pl.coopsoft.szambelan.core.utils

import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.Robolectric
import pl.coopsoft.testutils.EmptyTestActivity

@RunWith(AndroidJUnit4::class)
class GoogleSignInLauncherTests {

    private companion object {
        private const val TEST_GCP_ID = "abcd"
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EmptyTestActivity>()

    @Test
    fun testGoogleSignInLauncher() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        val onSignedIn = mock<(Boolean) -> Unit>()
        val onSuccess = mock<() -> Unit>()
        val onFailure = mock<() -> Unit>()
        val auth = Firebase.auth
        val googleSignInHelper = GoogleSignInHelper(auth)
        val googleSignInClient =
            spy(googleSignInHelper.googleSignInClient(composeTestRule.activity))

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

        verify(googleSignInClient).beginSignIn(any())

        Robolectric.flushForegroundThreadScheduler()
        Thread.sleep(100)
        Robolectric.flushForegroundThreadScheduler()

        verify(onFailure).invoke()
        verifyNoInteractions(onSuccess)
        verifyNoInteractions(onSignedIn)
    }
}