package pl.coopsoft.szambelan.domain.usecase.transfer

import android.util.Log
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.domain.model.DataModel
import pl.coopsoft.szambelan.domain.repository.firebase.DatabaseHelper
import pl.coopsoft.szambelan.presentation.dialogs.DialogData
import pl.coopsoft.szambelan.presentation.main.MainViewModel
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
    private val databaseHelper: DatabaseHelper
) {

    private companion object {
        private const val TAG = "DownloadUseCase"
    }

    fun askAndDownload(viewModel: MainViewModel, ok: (DataModel) -> Unit) {
        viewModel.displayDialog(
            DialogData(
                title = R.string.download_data,
                text = R.string.download_question,
                positiveButton = R.string.yes,
                negativeButton = R.string.cancel,
                positiveButtonCallback = {
                    downloadData(viewModel, ok)
                }
            )
        )
    }

    private fun downloadData(viewModel: MainViewModel, ok: (DataModel) -> Unit) {
        val dialog = DialogData(
            text = R.string.download_in_progress,
            negativeButton = R.string.cancel
        )
        viewModel.displayDialog(dialog)

        databaseHelper.downloadData { data ->
            viewModel.dismissDialog(dialog) {
                if (data != null) {
                    Log.i(TAG, "Data from remote storage downloaded successfully")
                    ok(data)
                } else {
                    Log.e(TAG, "Download from remote storage failed")
                }

                viewModel.displayDialog(
                    DialogData(
                        text = if (data != null) R.string.download_success else R.string.download_error,
                        positiveButton = R.string.ok
                    )
                )
            }
        }
    }

}