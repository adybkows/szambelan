package pl.coopsoft.szambelan.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import pl.coopsoft.szambelan.BuildConfig
import javax.inject.Inject

class EmailSignInHelper @Inject constructor(private val auth: FirebaseAuth) {

    private companion object {
        private const val TAG = "EmailSignInHelper"
    }

    fun emailSignIn(email: String, onEmailSent: (ok: Boolean) -> Unit) {
        val actionCodeSettings = actionCodeSettings {
            url = BuildConfig.BASE_URL
            handleCodeInApp = true
            setIOSBundleId(BuildConfig.APPLICATION_ID)
            setAndroidPackageName(BuildConfig.APPLICATION_ID, true, "1")
        }
        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "E-mail sent")
                } else {
                    Log.e(TAG, "Cannot send e-mail")
                }
                onEmailSent(task.isSuccessful)
            }
    }

    fun handleDeepLinks(
        email: String,
        intentData: Uri,
        onSignedIn: (ok: Boolean) -> Unit
    ): Boolean {
        val emailLink = intentData.toString()
        return if (auth.isSignInWithEmailLink(emailLink)) {
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link")
                    } else {
                        Log.e(TAG, "Error signing in with email link", task.exception)
                    }
                    onSignedIn(task.isSuccessful)
                }
            true
        } else {
            false
        }
    }

}