package pl.coopsoft.szambelan.domain.usecase.login

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.SignInClient
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.GoogleSignInHelper
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val googleSignInHelper: GoogleSignInHelper,
) {

    private lateinit var googleSignInClient: SignInClient
    private lateinit var googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    private var onComplete: ((ok: Boolean) -> Unit)? = null

    @SuppressLint("ComposableNaming")
    @Composable
    fun init() {
        googleSignInClient = googleSignInHelper.googleSignInClient(LocalContext.current as Activity)
        googleSignInLauncher = googleSignInHelper.googleSignInLauncher(googleSignInClient) {
            onComplete?.invoke(it)
        }
    }

    fun googleSignIn(activity: Activity, onComplete: ((ok: Boolean) -> Unit)?) {
        this.onComplete = onComplete
        googleSignInHelper.googleSignIn(
            activity, googleSignInClient, googleSignInLauncher, activity.getString(R.string.gcp_id)
        )
    }

}