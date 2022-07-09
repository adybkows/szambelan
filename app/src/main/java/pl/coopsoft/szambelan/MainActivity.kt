package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import pl.coopsoft.szambelan.ui.main.MainActivityScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadAllData()

        setContent {
            MainActivityScreen(
                prevEmptyActions = viewModel.prevEmptyActions,
                prevMainMeter = viewModel.prevMainMeter,
                onPrevMainMeterChange = {
                    viewModel.prevMainMeter.value = viewModel.updateDecimalSeparator(it)
                    viewModel.refreshCalculation()
                },
                prevGardenMeter = viewModel.prevGardenMeter,
                onPrevGardenMeterChange = {
                    viewModel.prevGardenMeter.value = viewModel.updateDecimalSeparator(it)
                    viewModel.refreshCalculation()
                },
                currentMainMeter = viewModel.currentMainMeter,
                onCurrentMainMeterChange = {
                    viewModel.currentMainMeter.value = viewModel.updateDecimalSeparator(it)
                    viewModel.refreshCalculation()
                },
                currentGardenMeter = viewModel.currentGardenMeter,
                onCurrentGardenMeterChange = {
                    viewModel.currentGardenMeter.value = viewModel.updateDecimalSeparator(it)
                    viewModel.refreshCalculation()
                },
                waterUsage = viewModel.waterUsage,
                daysLeft = viewModel.daysLeft,
                daysLeftColor = viewModel.daysLeftColor,
                emptyTankClicked = {
                    emptyTankClicked()
                }
            )
        }
    }

    override fun onBackPressed() {
        viewModel.saveEditValues()
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewModel.saveEditValues()
        super.onDestroy()
    }

    private fun emptyTankClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.empty_tank)
            .setMessage(R.string.empty_tank_question)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.emptyTank()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }
}