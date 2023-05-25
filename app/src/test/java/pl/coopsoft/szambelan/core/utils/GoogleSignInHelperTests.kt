package pl.coopsoft.szambelan.core.utils

import android.app.Activity
import android.app.PendingIntent
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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
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
        val activity = mock<Activity>()
        val task = mock<Task<BeginSignInResult>>()
        val googleSignInClient = mock<SignInClient>()
        val googleSignInLauncher =
            mock<ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>>()
        val successListenerCaptor = argumentCaptor<OnSuccessListener<BeginSignInResult>>()
        val failureListenerCaptor = argumentCaptor<OnFailureListener>()
        val auth = mock<FirebaseAuth>()
        val googleSignInHelper = GoogleSignInHelper(auth)

        whenever(googleSignInClient.beginSignIn(any())).thenReturn(task)
        whenever(task.addOnSuccessListener(any<Activity>(), any())).thenReturn(task)
        whenever(task.addOnFailureListener(any())).thenReturn(task)

        googleSignInHelper.googleSignIn(activity, googleSignInClient, googleSignInLauncher, TEST_GCP_ID)

        verify(googleSignInClient).beginSignIn(any())
        verify(task).addOnSuccessListener(eq(activity), successListenerCaptor.capture())
        verify(task).addOnFailureListener(eq(activity), failureListenerCaptor.capture())

        if (testSuccess) {
            val result = mock<BeginSignInResult>()
            val pendingIntent = mock<PendingIntent>()
            whenever(pendingIntent.intentSender).thenReturn(mock())
            whenever(result.pendingIntent).thenReturn(pendingIntent)
            successListenerCaptor.firstValue.onSuccess(result)
            verify(googleSignInLauncher).launch(any())
        } else {
            val exception = mock<Exception>()
            failureListenerCaptor.firstValue.onFailure(exception)
            verifyNoInteractions(googleSignInLauncher)
        }
    }
}