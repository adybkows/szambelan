package pl.coopsoft.szambelan.core.utils

import android.app.Activity
import android.app.PendingIntent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    fun testGoogleSignInSuccess() {
        testGoogleSignIn(true)
    }

    @Test
    fun testGoogleSignInFailure() {
        testGoogleSignIn(false)
    }

    private fun testGoogleSignIn(testSuccess: Boolean) {
        val activity = mockk<Activity>(relaxed = true)
        val task = mockk<Task<BeginSignInResult>>()
        val googleSignInClient = mockk<SignInClient>()
        val googleSignInLauncher =
            mockk<ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>>(relaxed = true)
        val successListenerSlot = slot<OnSuccessListener<BeginSignInResult>>()
        val failureListenerSlot = slot<OnFailureListener>()
        val auth = mockk<FirebaseAuth>()
        val googleSignInHelper = GoogleSignInHelper(auth)

        every { googleSignInClient.beginSignIn(any()) } returns task
        every { task.addOnSuccessListener(any<Activity>(), any()) } returns task
        every { task.addOnFailureListener(any<Activity>(), any()) } returns task

        googleSignInHelper.googleSignIn(activity, googleSignInClient, googleSignInLauncher, TEST_GCP_ID)

        verify { googleSignInClient.beginSignIn(any()) }
        verify { task.addOnSuccessListener(eq(activity), capture(successListenerSlot)) }
        verify { task.addOnFailureListener(eq(activity), capture(failureListenerSlot)) }

        if (testSuccess) {
            val result = mockk<BeginSignInResult>()
            val pendingIntent = mockk<PendingIntent>(relaxed = true)
            every { result.pendingIntent } returns pendingIntent
            successListenerSlot.captured.onSuccess(result)
            verify { googleSignInLauncher.launch(any()) }
        } else {
            val exception = mockk<Exception>(relaxed = true)
            failureListenerSlot.captured.onFailure(exception)
            verify(exactly = 0) { googleSignInLauncher.launch(any()) }
        }
    }
}
