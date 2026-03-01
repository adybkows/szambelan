package pl.coopsoft.szambelan.presentation.login

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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

    fun googleSignInClicked(context: Context, scope: CoroutineScope) {
        emailSent.value = false
        googleSignInUseCase.googleSignIn(context, scope) { _ ->
            // Navigation is handled by LoginStateUseCase in MainActivity
            // which listens to FirebaseAuth changes.
        }
    }

    fun handleDeepLinks(intent: Intent): Boolean {
        return emailLogInUseCase.handleDeepLinks(intent, null)
    }

}
