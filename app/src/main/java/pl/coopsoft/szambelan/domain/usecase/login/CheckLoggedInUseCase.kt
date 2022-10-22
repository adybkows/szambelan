package pl.coopsoft.szambelan.domain.usecase.login

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CheckLoggedInUseCase @Inject constructor(
    private val auth: FirebaseAuth
){

    fun isLoggedIn() =
        auth.currentUser != null

}