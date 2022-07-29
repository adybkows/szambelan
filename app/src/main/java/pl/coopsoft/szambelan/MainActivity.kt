package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.coopsoft.szambelan.ui.login.LoginScreen
import pl.coopsoft.szambelan.ui.main.MainScreen
import java.util.*

class MainActivity : ComponentActivity() {

    private companion object {
        private const val NAV_MAIN = "main"
        private const val NAV_LOGIN = "login"
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadAllData {
            if (viewModel.loggedIn.value) {
                Toast.makeText(
                    this,
                    if (it) R.string.download_success else R.string.download_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = NAV_MAIN) {
                composable(NAV_MAIN) { MainScreen(navController) }
                composable(NAV_LOGIN) { LoginScreen(navController) }
            }
        }
    }

    @Composable
    private fun MainScreen(navController: NavController) {
        MainScreen(
            loggedIn = viewModel.loggedIn.value,
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
            logInOutClicked = {
                if (!viewModel.loggedIn.value) {
                    navController.navigate(NAV_LOGIN)
                } else {
                    viewModel.loggedIn.value = false
                }
            }
        )
    }

    @Composable
    private fun LoginScreen(navController: NavController) {
        val id = remember { mutableStateOf(viewModel.userId) }
        LoginScreen(
            uniqueIdentifier = id.value,
            onUniqueIdentifierChange = {
                id.value = it
            },
            randomizeClicked = {
                id.value = UUID.randomUUID().toString()
            },
            logInClicked = {
                if (id.value.isNotEmpty()) {
                    navController.popBackStack()
                    viewModel.logIn(id.value) {
                        Toast.makeText(
                            this,
                            if (it) R.string.download_success else R.string.download_error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    override fun onBackPressed() {
        viewModel.saveEditValues()
        if (viewModel.loggedIn.value) {
            viewModel.uploadToRemoteStorage {
                Toast.makeText(
                    this,
                    if (it) R.string.upload_success else R.string.upload_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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