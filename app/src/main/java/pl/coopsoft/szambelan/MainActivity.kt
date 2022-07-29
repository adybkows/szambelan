package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import pl.coopsoft.szambelan.ui.main.MainScreen
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadAllData()

        setContent {
            MainScreen(
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
                },
                downloadClicked = {
                    viewModel.downloadFromRemoteStorage {
                        if (it) {
                            viewModel.showMeterStates()
                            viewModel.refreshCalculation()
                            viewModel.saveEditValues()
                            viewModel.saveMeterStates()
                        }
                        Toast.makeText(
                            this,
                            if (it) android.R.string.ok else android.R.string.httpErrorBadUrl,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                uploadClicked = {
                    viewModel.uploadToRemoteStorage {
                        Toast.makeText(
                            this,
                            if (it) android.R.string.ok else android.R.string.httpErrorBadUrl,
                            Toast.LENGTH_LONG
                        ).show()
                    }
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