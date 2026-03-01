package pl.coopsoft.szambelan.domain.usecase.login

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.GoogleSignInHelper
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val googleSignInHelper: GoogleSignInHelper,
) {

    fun googleSignIn(context: Context, scope: CoroutineScope, onComplete: ((ok: Boolean) -> Unit)?) {
        googleSignInHelper.googleSignIn(
            context = context,
            scope = scope,
            gcpId = context.getString(R.string.gcp_id),
            onSignedIn = { ok -> onComplete?.invoke(ok) },
            onFailure = { onComplete?.invoke(false) }
        )
    }
}
