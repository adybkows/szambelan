package pl.coopsoft.szambelan.domain.usecase.login

import android.content.Intent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import pl.coopsoft.szambelan.core.utils.EmailSignInHelper
import pl.coopsoft.szambelan.core.utils.Persistence
import javax.inject.Inject

class EmailLogInUseCase @Inject constructor(
    private val emailSignInHelper: EmailSignInHelper,
    private val persistence: Persistence
) {

    fun emailLogin(email: String, onComplete: (ok: Boolean) -> Unit) {
        if (email.isNotEmpty()) {
            emailSignInHelper.emailSignIn(email) { ok ->
                if (ok) {
                    runBlocking {
                        persistence.setUserEmail(email)
                    }
                }
                onComplete(ok)
            }
        }
    }

    fun handleDeepLinks(intent: Intent, onLoggedInSuccessfully: (() -> Unit)?): Boolean {
        val intentData = intent.data
        if (intentData != null) {
            val email = runBlocking {
                persistence.userEmailFlow.first()
            }
            if (email.isNotEmpty()) {
                return emailSignInHelper.handleDeepLinks(email, intentData) {
                    if (it) {
                        onLoggedInSuccessfully?.invoke()
                    }
                }
            }
        }
        return false
    }

}
