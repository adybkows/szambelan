package pl.coopsoft.szambelan.domain.usecase.tank

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.core.utils.DialogUtils
import javax.inject.Inject

class EmptyTankUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dialogUtils: DialogUtils
) {

    fun askToEmptyTank(ok: () -> Unit) {
        dialogUtils.showQuestionDialog(
            context = context,
            title = R.string.empty_tank,
            message = R.string.empty_tank_question,
            yesClicked = {
                ok()
            }
        )
    }

}