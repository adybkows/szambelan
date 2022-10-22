package pl.coopsoft.szambelan.domain.usecase.login

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.coopsoft.szambelan.core.utils.EmailSignInHelper
import pl.coopsoft.szambelan.core.utils.Persistence
import javax.inject.Inject

class EmailLogInUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emailSignInHelper: EmailSignInHelper,
    private val persistence: Persistence
) {

    fun emailLogin(email: String, onComplete: (ok: Boolean) -> Unit) {
        if (email.isNotEmpty()) {
            emailSignInHelper.emailSignIn(email) {
                if (it) {
                    persistence.putString(context, Persistence.PREF_USER_EMAIL, email)
                }
                onComplete(it)
            }
        }
    }

    fun handleDeepLinks(intent: Intent, onLoggedInSuccessfully: (() -> Unit)?): Boolean {
        val intentData = intent.data
        if (intentData != null) {
            val email = persistence.getString(context, Persistence.PREF_USER_EMAIL, "")
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