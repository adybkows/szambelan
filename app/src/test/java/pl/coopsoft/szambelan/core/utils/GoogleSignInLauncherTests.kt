package pl.coopsoft.szambelan.core.utils

import android.os.Build
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
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
        val onFailure = mockk<() -> Unit>(relaxed = true)
        val auth = Firebase.auth
        val googleSignInHelper = GoogleSignInHelper(auth)

        composeTestRule.activity.setContent {
            val scope = rememberCoroutineScope()
            Button(onClick = {
                googleSignInHelper.googleSignIn(
                    context = composeTestRule.activity,
                    scope = scope,
                    gcpId = TEST_GCP_ID,
                    onSignedIn = onSignedIn,
                    onFailure = onFailure
                )
            }) {
                Text("OK")
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").performClick()

        ShadowLooper.idleMainLooper()
        // Since we can't easily mock CredentialManager in Robolectric without 
        // complex setups, we are just verifying the call doesn't crash the test.
        // In a real scenario, this would trigger the Credential Manager UI.
    }
}
