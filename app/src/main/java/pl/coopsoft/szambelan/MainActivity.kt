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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pl.coopsoft.szambelan.ui.login.LoginScreen
import pl.coopsoft.szambelan.ui.main.MainScreen
import pl.coopsoft.szambelan.utils.EmailSignInHelper
import pl.coopsoft.szambelan.utils.GoogleSignInHelper


class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
        private const val NAV_MAIN = "main"
        private const val NAV_LOGIN = "login"
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadSavedData()

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = NAV_MAIN) {
                composable(NAV_MAIN) { MainScreen(navController) }
                composable(NAV_LOGIN) { LoginScreen(navController) }
            }
        }

        intent.data?.let { intentData ->
            val email = Persistence.getString(this, Persistence.PREF_USER_EMAIL, "")
            if (email.isNotEmpty()) {
                EmailSignInHelper.handleDeepLinks(email, intentData) {
                    if (it) {
                        loggedInSuccessfully()
                    }
                }
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
            daysSince = viewModel.daysSince,
            daysLeft = viewModel.daysLeft,
            daysLeftColor = viewModel.daysLeftColor,
            emptyTankClicked = {
                emptyTankClicked()
            },
            logInOutClicked = {
                if (!viewModel.loggedIn.value) {
                    navController.navigate(NAV_LOGIN)
                } else {
                    Firebase.auth.signOut()
                    viewModel.loggedIn.value = false
                }
            },
            downloadClicked = {
                downloadClicked()
            },
            uploadClicked = {
                uploadClicked()
            }
        )
    }

    @Composable
    private fun LoginScreen(navController: NavController) {
        val email = remember { mutableStateOf("") }
        val emailSent = remember { mutableStateOf(false) }
        val googleSignInClient = GoogleSignInHelper.googleSignInClient(this)
        val googleSignInLauncher = GoogleSignInHelper.googleSignInLauncher(googleSignInClient) {
            if (it) {
                loggedInSuccessfully()
                navController.popBackStack()
            }
        }
        LoginScreen(
            email = email.value,
            onEmailChange = {
                email.value = it
            },
            emailSent = emailSent.value,
            emailLogInClicked = {
                if (email.value.isNotEmpty()) {
                    EmailSignInHelper.emailSignIn(email.value) {
                        if (it) {
                            emailSent.value = true
                            Persistence.putString(this, Persistence.PREF_USER_EMAIL, email.value)
                        }
                    }
                }
            },
            googleSignInClicked = {
                emailSent.value = false
                GoogleSignInHelper.googleSignIn(
                    this, googleSignInClient, googleSignInLauncher, getString(R.string.gcp_id)
                )
            }
        )
    }

    override fun onBackPressed() {
        viewModel.saveEditValues()
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewModel.saveEditValues()
        super.onDestroy()
    }

    private fun downloadClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.download_data)
            .setMessage(R.string.download_question)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.downloadFromRemoteStorage {
                    if (it) {
                        viewModel.showMeterStates()
                        viewModel.refreshCalculation()
                        viewModel.saveEditValues()
                        viewModel.saveMeterStates()
                    }
                    Toast.makeText(
                        this,
                        if (it) R.string.download_success else R.string.download_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.upload_data)
            .setMessage(R.string.upload_question)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.uploadToRemoteStorage {
                    Toast.makeText(
                        this,
                        if (it) R.string.upload_success else R.string.upload_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
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

    private fun loggedInSuccessfully() {
        viewModel.loggedIn.value = true
    }
}