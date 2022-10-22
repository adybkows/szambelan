package pl.coopsoft.szambelan.domain.usecase.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class LoginStateUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {

    private var authStateListener: AuthStateListener? = null

    fun addLoginStateListener(onStateChanged: (loggedIn: Boolean) -> Unit) {
        removeLoginStateListener()

        authStateListener = AuthStateListener {
            onStateChanged(it.currentUser != null)
        }

        authStateListener?.let {
            auth.addAuthStateListener(it)
        }
    }

    fun removeLoginStateListener() {
        authStateListener?.let {
            auth.removeAuthStateListener(it)
            authStateListener = null
        }
    }
}