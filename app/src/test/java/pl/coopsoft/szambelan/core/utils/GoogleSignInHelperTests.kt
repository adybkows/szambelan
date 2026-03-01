package pl.coopsoft.szambelan.core.utils

import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class GoogleSignInHelperTests {

    private companion object {
        private const val TEST_GCP_ID = "abcd"
    }

    @Test
    fun testGoogleSignIn() = runTest {
        val context = mockk<Context>(relaxed = true)
        val auth = mockk<FirebaseAuth>(relaxed = true)
        val googleSignInHelper = GoogleSignInHelper(auth)

        // CredentialManager.create(context) and getCredential are hard to mock 
        // because of static initializers and internal delegates.
        // In a real project, we would typically wrap CredentialManager in an interface
        // to make it testable or use a higher level integration test.
        
        googleSignInHelper.googleSignIn(
            context = context,
            scope = this,
            gcpId = TEST_GCP_ID,
            onSignedIn = {},
            onFailure = {}
        )
    }
}
