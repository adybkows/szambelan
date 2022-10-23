package pl.coopsoft.szambelan.presentation.dialogs

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.presentation.theme.MainTheme

@Composable
fun MyAlertDialog(
    @StringRes title: Int? = null,
    @StringRes text: Int? = null,
    @StringRes positiveButton: Int? = null,
    @StringRes negativeButton: Int? = null,
    positiveButtonCallback: (() -> Unit)? = null,
    negativeButtonCallback: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        //containerColor = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()),
        onDismissRequest = {
            onDismiss?.invoke()
        },
        title = {
            title?.let {
                Text(text = stringResource(it))
            }
        },
        text = {
            text?.let {
                Text(text = stringResource(it))
            }
        },
        confirmButton = {
            positiveButton?.let {
                TextButton(
                    onClick = {
                        positiveButtonCallback?.invoke()
                    }
                ) {
                    Text(stringResource(it))
                }
            }
        },
        dismissButton = {
            negativeButton?.let {
                TextButton(
                    onClick = {
                        negativeButtonCallback?.invoke()
                    }
                ) {
                    Text(stringResource(it))
                }
            }
        }
    )
}

@Preview
@Composable
fun QuestionDialogPreview() {
    MainTheme {
        MyAlertDialog(
            R.string.empty_tank, R.string.empty_tank_question, R.string.yes, R.string.cancel
        )
    }
}

@Preview
@Composable
fun ProgressDialogPreview() {
    MainTheme {
        MyAlertDialog(
            text = R.string.download_in_progress, negativeButton = R.string.cancel
        )
    }
}

@Preview
@Composable
fun OKDialogPreview() {
    MainTheme {
        MyAlertDialog(
            text = R.string.download_success, positiveButton = R.string.ok
        )
    }
}
