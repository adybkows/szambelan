package pl.coopsoft.szambelan.core.utils

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoogleSignInHelper @Inject constructor(private val auth: FirebaseAuth) {

    private companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    fun googleSignIn(
        context: Context,
        scope: CoroutineScope,
        gcpId: String,
        onSignedIn: (ok: Boolean) -> Unit,
        onFailure: (() -> Unit)? = null
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(gcpId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                Log.d(TAG, "Launching Credential Manager UI...")
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                val credential = result.credential

                Log.d(TAG, "Received credential type: ${credential.type}")

                // Be more flexible with type checking
                val isGoogleId =
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                            credential.type.contains("googleid", ignoreCase = true)

                if (isGoogleId) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Successfully parsed GoogleIdTokenCredential")

                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                        Log.d(TAG, "Signing in to Firebase...")
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Firebase sign-in SUCCESSFUL")
                                } else {
                                    Log.e(TAG, "Firebase sign-in FAILED", task.exception)
                                }
                                onSignedIn(task.isSuccessful)
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse Google ID token from credential data", e)
                        onSignedIn(false)
                    }
                } else {
                    Log.e(TAG, "Received unexpected credential type: ${credential.type}")
                    onSignedIn(false)
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error: [${e.type}] ${e.message}")
                onFailure?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during sign-in: ${e.localizedMessage}", e)
                onFailure?.invoke()
            }
        }
    }
}
