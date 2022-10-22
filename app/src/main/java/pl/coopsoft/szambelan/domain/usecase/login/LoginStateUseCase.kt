package pl.coopsoft.szambelan.domain.usecase.login

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginStateUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {

    fun addLoginStateListener(onStateChanged: (loggedIn: Boolean) -> Unit) {
        auth.addAuthStateListener {
            onStateChanged(it.currentUser != null)
        }
    }

}