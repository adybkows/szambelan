package pl.coopsoft.szambelan.domain.usecase.login

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {

    fun logOut() {
        auth.signOut()
    }

}