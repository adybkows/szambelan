package pl.coopsoft.szambelan.core.utils

import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
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
        val onEmailSent = mockk<(Boolean) -> Unit>(relaxed = true)
        val auth = mockk<FirebaseAuth>()
        val task = mockk<Task<Void>>()
        val completeListenerSlot = slot<OnCompleteListener<Void>>()
        val emailSignInHelper = EmailSignInHelper(auth)
        
        every { auth.sendSignInLinkToEmail(any(), any()) } returns task
        every { task.addOnCompleteListener(capture(completeListenerSlot)) } returns task
        every { task.isSuccessful } returns testSuccess

        emailSignInHelper.emailSignIn(TEST_EMAIL, onEmailSent)

        verify { auth.sendSignInLinkToEmail(eq(TEST_EMAIL), any()) }
        verify { task.addOnCompleteListener(any()) }

        completeListenerSlot.captured.onComplete(task)
        verify { onEmailSent.invoke(eq(testSuccess)) }
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
        val onSignedIn = mockk<(Boolean) -> Unit>(relaxed = true)
        val auth = mockk<FirebaseAuth>()
        val emailSignInHelper = EmailSignInHelper(auth)

        every { auth.isSignInWithEmailLink(any()) } returns false

        assertFalse(emailSignInHelper.handleDeepLinks("", Uri.parse(""), onSignedIn))
    }

    private fun testHandleDeepLinks(testSuccess: Boolean) {
        val onSignedIn = mockk<(Boolean) -> Unit>(relaxed = true)
        val auth = mockk<FirebaseAuth>()
        val task = mockk<Task<AuthResult>>()
        val completeListenerSlot = slot<OnCompleteListener<AuthResult>>()
        val emailSignInHelper = EmailSignInHelper(auth)

        every { auth.isSignInWithEmailLink(eq(TEST_URI)) } returns true
        every { auth.signInWithEmailLink(any(), any()) } returns task
        every { task.addOnCompleteListener(capture(completeListenerSlot)) } returns task
        every { task.isSuccessful } returns testSuccess
        every { task.exception } returns if (!testSuccess) Exception("test") else null

        assertTrue(emailSignInHelper.handleDeepLinks(TEST_EMAIL, Uri.parse(TEST_URI), onSignedIn))
        verify { auth.signInWithEmailLink(eq(TEST_EMAIL), eq(TEST_URI)) }
        verify { task.addOnCompleteListener(any()) }

        completeListenerSlot.captured.onComplete(task)
        verify { onSignedIn.invoke(eq(testSuccess)) }
    }
}
