package pl.coopsoft.szambelan.presentation.dialogs

import androidx.annotation.StringRes

data class DialogData(
    @StringRes val title: Int? = null,
    @StringRes val text: Int? = null,
    @StringRes val positiveButton: Int? = null,
    @StringRes val negativeButton: Int? = null,
    val positiveButtonCallback: (() -> Unit)? = null,
    val negativeButtonCallback: (() -> Unit)? = null,
    val onDismissCallback: (() -> Unit)? = null
)