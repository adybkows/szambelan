package pl.coopsoft.szambelan.presentation.login

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.coopsoft.szambelan.domain.usecase.login.EmailLogInUseCase
import pl.coopsoft.szambelan.domain.usecase.login.GoogleSignInUseCase

@RunWith(AndroidJUnit4::class)
class LoginViewModelTests {

    private companion object {
        private const val TEST_EMAIL = "abcd@ef.gh"
    }

    private val emailLogInUseCase = mockk<EmailLogInUseCase>()
    private val googleSignInUseCase = mockk<GoogleSignInUseCase>()
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        loginViewModel = LoginViewModel(application, emailLogInUseCase, googleSignInUseCase)
    }

    @Test
    fun testEmailLogInClickedSuccess() {
        testEmailLogInClicked(true)
    }

    @Test
    fun testEmailLogInClickedFailure() {
        testEmailLogInClicked(false)
    }

    private fun testEmailLogInClicked(testSuccess: Boolean) {
        val onCompleteSlot = slot<(Boolean) -> Unit>()
        justRun { emailLogInUseCase.emailLogin(TEST_EMAIL, capture(onCompleteSlot)) }

        loginViewModel.email.value = TEST_EMAIL
        loginViewModel.emailSent.value = false
        loginViewModel.emailLogInClicked()

        assertThat(onCompleteSlot.captured).isNotNull()

        onCompleteSlot.captured.invoke(testSuccess)
        assertThat(loginViewModel.emailSent.value).isEqualTo(testSuccess)
    }

    @Test
    fun testGoogleSignInClicked() {
        val activity = mockk<Activity>()
        justRun { googleSignInUseCase.googleSignIn(any(), any()) }
        loginViewModel.emailSent.value = true
        loginViewModel.googleSignInClicked(activity)

        assertThat(loginViewModel.emailSent.value).isFalse()
        verify { googleSignInUseCase.googleSignIn(activity, null) }
    }

    @Test
    fun testHandleDeepLinksHandled() {
        val intent = mockk<Intent>()
        every { emailLogInUseCase.handleDeepLinks(any(), any()) } returns true
        assertThat(loginViewModel.handleDeepLinks(intent)).isTrue()
        verify { emailLogInUseCase.handleDeepLinks(intent, any()) }
    }

    @Test
    fun testHandleDeepLinksNotHandled() {
        val intent = mockk<Intent>()
        every { emailLogInUseCase.handleDeepLinks(any(), any()) } returns false
        assertThat(loginViewModel.handleDeepLinks(intent)).isFalse()
        verify { emailLogInUseCase.handleDeepLinks(intent, any()) }
    }
}