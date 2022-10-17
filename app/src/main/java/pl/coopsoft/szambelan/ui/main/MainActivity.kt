package pl.coopsoft.szambelan.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.ui.login.LoginScreen
import pl.coopsoft.szambelan.utils.DialogUtils
import pl.coopsoft.szambelan.utils.EmailSignInHelper
import pl.coopsoft.szambelan.utils.GoogleSignInHelper
import pl.coopsoft.szambelan.utils.Persistence
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
        private const val NAV_MAIN = "main"
        private const val NAV_LOGIN = "login"
    }

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var emailSignInHelper: EmailSignInHelper

    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper

    @Inject
    lateinit var persistence: Persistence

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
            val email = persistence.getString(this, Persistence.PREF_USER_EMAIL, "")
            if (email.isNotEmpty()) {
                emailSignInHelper.handleDeepLinks(email, intentData) {
                    if (it) {
                        loggedInSuccessfully()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.saveEditValues()
                finish()
            }
        })
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
                    auth.signOut()
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
        val googleSignInClient = googleSignInHelper.googleSignInClient(this)
        val googleSignInLauncher = googleSignInHelper.googleSignInLauncher(googleSignInClient) {
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
                    emailSignInHelper.emailSignIn(email.value) {
                        if (it) {
                            emailSent.value = true
                            persistence.putString(this, Persistence.PREF_USER_EMAIL, email.value)
                        }
                    }
                }
            },
            googleSignInClicked = {
                emailSent.value = false
                googleSignInHelper.googleSignIn(
                    this, googleSignInClient, googleSignInLauncher, getString(R.string.gcp_id)
                )
            }
        )
    }

    private fun downloadClicked() {
        dialogUtils.showQuestionDialog(
            context = this,
            title = R.string.download_data,
            message = R.string.download_question,
            yesClicked = {
                downloadData()
            }
        )
    }

    private fun downloadData() {
        val dialog = dialogUtils.showInProgressDialog(this, R.string.download_in_progress)
        viewModel.downloadFromRemoteStorage {
            dialog.dismiss()
            if (it) {
                viewModel.showMeterStates()
                viewModel.refreshCalculation()
                viewModel.saveEditValues()
                viewModel.saveMeterStates()
            }
            dialogUtils.showOKDialog(
                this,
                if (it) R.string.download_success else R.string.download_error
            )
        }
    }

    private fun uploadClicked() {
        dialogUtils.showQuestionDialog(
            context = this,
            title = R.string.upload_data,
            message = R.string.upload_question,
            yesClicked = {
                uploadData()
            }
        )
    }

    private fun uploadData() {
        val dialog = dialogUtils.showInProgressDialog(this, R.string.upload_in_progress)
        viewModel.uploadToRemoteStorage {
            dialog.dismiss()
            dialogUtils.showOKDialog(
                this,
                if (it) R.string.upload_success else R.string.upload_error
            )
        }
    }

    private fun emptyTankClicked() {
        dialogUtils.showQuestionDialog(
            context = this,
            title = R.string.empty_tank,
            message = R.string.empty_tank_question,
            yesClicked = {
                viewModel.emptyTank()
            }
        )
    }

    private fun loggedInSuccessfully() {
        viewModel.loggedIn.value = true
    }
}