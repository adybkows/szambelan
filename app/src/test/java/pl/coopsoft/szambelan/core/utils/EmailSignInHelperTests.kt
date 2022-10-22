package pl.coopsoft.szambelan.core.utils

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EmailSignInHelperTests {

    private companion object {
        private const val TEST_EMAIL = "abcd@ef.gh"
        private const val TEST_URI = "https://host.domain/path"
    }

    @Test
    fun testEmailSignInSuccess() {
        testEmailSignIn(true)
    }

    @Test
    fun testEmailSignInFailure() {
        testEmailSignIn(false)
    }

    private fun testEmailSignIn(testSuccess: Boolean) {
        val onEmailSent = mock<(Boolean) -> Unit>()
        val auth = mock<FirebaseAuth>()
        val task = mock<Task<Void>>()
        val completeListenerCaptor = argumentCaptor<OnCompleteListener<Void>>()
        val emailSignInHelper = EmailSignInHelper(auth)
        whenever(auth.sendSignInLinkToEmail(any(), any())).thenReturn(task)

        emailSignInHelper.emailSignIn(TEST_EMAIL, onEmailSent)

        verify(auth).sendSignInLinkToEmail(eq(TEST_EMAIL), any())
        verify(task).addOnCompleteListener(completeListenerCaptor.capture())

        whenever(task.isSuccessful).thenReturn(testSuccess)
        completeListenerCaptor.firstValue.onComplete(task)
        verify(onEmailSent).invoke(eq(testSuccess))
    }

    @Test
    fun testHandleDeepLinksSuccess() {
        testHandleDeepLinks(true)
    }

    @Test
    fun testHandleDeepLinksFailure() {
        testHandleDeepLinks(false)
    }

    @Test
    fun testHandleDeepLinksBadLink() {
        val onSignedIn = mock<(Boolean) -> Unit>()
        val auth = mock<FirebaseAuth>()
        val emailSignInHelper = EmailSignInHelper(auth)

        whenever(auth.isSignInWithEmailLink(any())).thenReturn(false)

        assertFalse(emailSignInHelper.handleDeepLinks("", Uri.parse(""), onSignedIn))
    }

    private fun testHandleDeepLinks(testSuccess: Boolean) {
        val onSignedIn = mock<(Boolean) -> Unit>()
        val auth = mock<FirebaseAuth>()
        val task = mock<Task<AuthResult>>()
        val completeListenerCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
        val emailSignInHelper = EmailSignInHelper(auth)

        whenever(auth.isSignInWithEmailLink(eq(TEST_URI))).thenReturn(true)
        whenever(auth.signInWithEmailLink(any(), any())).thenReturn(task)

        assertTrue(emailSignInHelper.handleDeepLinks(TEST_EMAIL, Uri.parse(TEST_URI), onSignedIn))
        verify(auth).signInWithEmailLink(eq(TEST_EMAIL), eq(TEST_URI))
        verify(task).addOnCompleteListener(completeListenerCaptor.capture())

        whenever(task.isSuccessful).thenReturn(testSuccess)
        completeListenerCaptor.firstValue.onComplete(task)
        verify(onSignedIn).invoke(eq(testSuccess))
    }
}