package pl.coopsoft.szambelan.presentation.dialogs

import androidx.annotation.StringRes

data class DialogData(
    @param:StringRes val title: Int? = null,
    @param:StringRes val text: Int? = null,
    @param:StringRes val positiveButton: Int? = null,
    @param:StringRes val negativeButton: Int? = null,
    val positiveButtonCallback: (() -> Unit)? = null,
    val negativeButtonCallback: (() -> Unit)? = null,
    val onDismissCallback: (() -> Unit)? = null
)