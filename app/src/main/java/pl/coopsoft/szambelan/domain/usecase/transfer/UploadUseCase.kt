package pl.coopsoft.szambelan.domain.usecase.transfer

import android.content.Context
import android.util.Log
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.DialogUtils
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.repository.firebase.DatabaseHelper
import javax.inject.Inject

class UploadUseCase @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val dialogUtils: DialogUtils
) {

    private companion object {
        private const val TAG = "UploadUseCase"
    }

    fun askAndUpload(context: Context, data: DataModel) {
        dialogUtils.showQuestionDialog(context = context,
            title = R.string.upload_data,
            message = R.string.upload_question,
            yesClicked = {
                uploadData(context, data)
            })
    }

    private fun uploadData(context: Context, data: DataModel) {
        val dialog = dialogUtils.showInProgressDialog(context, R.string.upload_in_progress)

        databaseHelper.uploadData(data) { t ->
            dialog.dismiss()
            if (t != null) {
                Log.e(TAG, "Upload failed: $t")
            } else {
                Log.i(TAG, "Data uploaded successfully to remote storage")
            }
            dialogUtils.showOKDialog(
                context, if (t == null) R.string.upload_success else R.string.upload_error
            )
        }
    }

}