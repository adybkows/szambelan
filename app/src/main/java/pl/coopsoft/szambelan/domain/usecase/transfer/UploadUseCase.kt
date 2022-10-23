package pl.coopsoft.szambelan.domain.usecase.transfer

import android.util.Log
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.repository.firebase.DatabaseHelper
import pl.coopsoft.szambelan.presentation.dialogs.DialogData
import pl.coopsoft.szambelan.presentation.main.MainViewModel
import javax.inject.Inject

class UploadUseCase @Inject constructor(
    private val databaseHelper: DatabaseHelper
) {

    private companion object {
        private const val TAG = "UploadUseCase"
    }

    fun askAndUpload(viewModel: MainViewModel, data: DataModel) {
        viewModel.displayDialog(
            DialogData(
                title = R.string.upload_data,
                text = R.string.upload_question,
                positiveButton = R.string.yes,
                negativeButton = R.string.cancel,
                positiveButtonCallback = {
                    uploadData(viewModel, data)
                }
            )
        )
    }

    private fun uploadData(viewModel: MainViewModel, data: DataModel) {
        val dialog = DialogData(
            text = R.string.upload_in_progress,
            negativeButton = R.string.cancel
        )
        viewModel.displayDialog(dialog)

        databaseHelper.uploadData(data) { t ->
            viewModel.dismissDialog(dialog) {
                if (t != null) {
                    Log.e(TAG, "Upload failed: $t")
                } else {
                    Log.i(TAG, "Data uploaded successfully to remote storage")
                }

                viewModel.displayDialog(
                    DialogData(
                        text = if (t == null) R.string.upload_success else R.string.upload_error,
                        positiveButton = R.string.ok
                    )
                )
            }
        }
    }

}