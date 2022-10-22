package pl.coopsoft.szambelan.domain.usecase.transfer

import android.content.Context
import android.util.Log
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.DialogUtils
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.repository.firebase.DatabaseHelper
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val dialogUtils: DialogUtils
) {

    private companion object {
        private const val TAG = "DownloadUseCase"
    }

    fun askAndDownload(context: Context, ok: (DataModel) -> Unit) {
        dialogUtils.showQuestionDialog(
            context = context,
            title = R.string.download_data,
            message = R.string.download_question,
            yesClicked = {
                downloadData(context, ok)
            })
    }

    private fun downloadData(context: Context, ok: (DataModel) -> Unit) {
        val dialog = dialogUtils.showInProgressDialog(context, R.string.download_in_progress)
        databaseHelper.downloadData { data ->
            dialog.dismiss()
            if (data != null) {
                Log.i(TAG, "Data from remote storage downloaded successfully")
                ok(data)
            } else {
                Log.e(TAG, "Download from remote storage failed")
            }
            dialogUtils.showOKDialog(
                context, if (data != null) R.string.download_success else R.string.download_error
            )
        }
    }

}