package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

class DialogUtils @Inject constructor() {

    fun showQuestionDialog(
        context: Context, @StringRes title: Int, @StringRes message: Int,
        yesClicked: () -> Unit, cancelClicked: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                yesClicked()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                cancelClicked?.invoke()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
                cancelClicked?.invoke()
            }
            .show()
    }

    fun showInProgressDialog(
        context: Context, @StringRes message: Int, cancelClicked: (() -> Unit)? = null
    ): AlertDialog =
        AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                cancelClicked?.invoke()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
                cancelClicked?.invoke()
            }
            .show()

    fun showOKDialog(context: Context, @StringRes message: Int) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }

}