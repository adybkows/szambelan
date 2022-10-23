package pl.coopsoft.szambelan.presentation.main

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.presentation.theme.MainTheme

@Composable
fun MyAlertDialog(
    showDialog: MutableState<Boolean>,
    @StringRes title: Int,
    @StringRes text: Int,
    onOK: () -> Unit,
    onCancel: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
            onDismiss?.invoke()
        },
        title = {
            Text(text = stringResource(title))
        },
        text = {
            Text(text = stringResource(text))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    onOK()
                }
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    onCancel?.invoke()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun MyAlertDialogPreview() {
    MainTheme {
        MyAlertDialog(
            mutableStateOf(true), R.string.empty_tank, text = R.string.empty_tank_question, {}
        )
    }
}