package pl.coopsoft.szambelan.presentation.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
fun DisplayDialogs(dialogs: SnapshotStateList<DialogData>, dismissDialog: (DialogData, (() -> Unit)?) -> Unit) {
    dialogs.forEach {
        MyAlertDialog(
            title = it.title,
            text = it.text,
            positiveButton = it.positiveButton,
            negativeButton = it.negativeButton,
            positiveButtonCallback = {
                dismissDialog(it) {
                    it.positiveButtonCallback?.invoke()
                }
            },
            negativeButtonCallback = {
                dismissDialog(it) {
                    it.negativeButtonCallback?.invoke()
                }
            },
            onDismiss = {
                dismissDialog(it) {
                    it.onDismissCallback?.invoke()
                }
            }
        )
    }
}
