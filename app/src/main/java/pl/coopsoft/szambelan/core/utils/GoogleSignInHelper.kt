package pl.coopsoft.szambelan.core.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

class GoogleSignInHelper @Inject constructor(private val auth: FirebaseAuth) {

    private companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    fun googleSignInClient(activity: Activity) =
        Identity.getSignInClient(activity)

    @Composable
    fun googleSignInLauncher(googleSignInClient: SignInClient, onSignedIn: (ok: Boolean) -> Unit) =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val googleCredential = googleSignInClient.getSignInCredentialFromIntent(result.data)
                val idToken = googleCredential.googleIdToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        onSignedIn(task.isSuccessful)
                    }
            }
        }

    fun googleSignIn(
        activity: Activity,
        googleSignInClient: SignInClient,
        googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        gcpId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: (() -> Unit)? = null
    ) {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(gcpId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        googleSignInClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity) { result ->
                try {
                    googleSignInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                    onSuccess?.invoke()
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(activity) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage.orEmpty())
                onFailure?.invoke()
            }
    }
}