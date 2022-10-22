package pl.coopsoft.szambelan.presentation.login

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.coopsoft.szambelan.domain.usecase.login.EmailLogInUseCase
import pl.coopsoft.szambelan.domain.usecase.login.GoogleSignInUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val emailLogInUseCase: EmailLogInUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase
) : AndroidViewModel(application) {

    val email = mutableStateOf("")
    val emailSent = mutableStateOf(false)

    fun emailLogInClicked() {
        emailLogInUseCase.emailLogin(email.value) {
            if (it) {
                emailSent.value = true
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun googleSignInInit() {
        googleSignInUseCase.init()
    }

    fun googleSignInClicked(activity: Activity) {
        emailSent.value = false
        googleSignInUseCase.googleSignIn(activity, null)
    }

    fun handleDeepLinks(intent: Intent): Boolean {
        return emailLogInUseCase.handleDeepLinks(intent, null)
    }

}