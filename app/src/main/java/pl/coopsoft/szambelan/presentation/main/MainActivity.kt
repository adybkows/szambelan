package pl.coopsoft.szambelan.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.coopsoft.szambelan.core.utils.FormattingUtils
import pl.coopsoft.szambelan.domain.usecase.login.LoginStateUseCase
import pl.coopsoft.szambelan.presentation.NavScreens
import pl.coopsoft.szambelan.presentation.login.LoginScreen
import pl.coopsoft.szambelan.presentation.login.LoginViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var navController: NavHostController

    @Inject
    lateinit var formattingUtils: FormattingUtils

    @Inject
    lateinit var loginStateUseCase: LoginStateUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadSavedData()

        setContent {
            navController = rememberNavController()

            NavHost(navController = navController, startDestination = NavScreens.MAIN) {
                composable(NavScreens.MAIN) {
                    MainScreen(
                        navController = navController,
                        viewModel = viewModel,
                        formattingUtils = formattingUtils
                    )
                }
                composable(NavScreens.LOGIN) { LoginScreen(loginViewModel) }
            }
        }

        if (intent.data != null) {
            loginViewModel.handleDeepLinks(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.saveEditValues()
                finish()
            }
        })

        loginStateUseCase.addLoginStateListener { loggedIn ->
            viewModel.loggedIn.value = loggedIn
            if (loggedIn && ::navController.isInitialized) {
                navController.popBackStack(route = NavScreens.MAIN, inclusive = false)
            }
        }
    }

    override fun onDestroy() {
        loginStateUseCase.removeLoginStateListener()
        super.onDestroy()
    }
}