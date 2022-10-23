package pl.coopsoft.szambelan.presentation.login

import android.app.Activity
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.presentation.theme.Grey44p
import pl.coopsoft.szambelan.presentation.theme.MainTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    emailSent: Boolean,
    emailLogInClicked: () -> Unit,
    googleSignInClicked: () -> Unit
) {
    val showGoogleSignIn = remember { mutableStateOf(true) }
    val showEmailSignIn = remember { mutableStateOf(true) }
    MainTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(
                            if (showGoogleSignIn.value) 1.0f else 0.25f
                        ), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            if (showGoogleSignIn.value) {
                                showEmailSignIn.value = false
                                googleSignInClicked()
                            }
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_google),
                            contentDescription = ""
                        )
                        Text(
                            text = stringResource(R.string.sign_with_google),
                            modifier = Modifier.padding(start = 16.dp),
                            color = Grey44p,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp
                        )
                    }
                    if (showEmailSignIn.value) {
                        Text(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.or)
                        )
                    }
                }
                if (showEmailSignIn.value) {
                    Text(
                        modifier = Modifier.padding(top = 32.dp),
                        text = stringResource(R.string.enter_email_address)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = email,
                        onValueChange = { value ->
                            if (value != email) {
                                onEmailChange(value)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email, autoCorrect = true
                        )
                    )
                    Button(
                        enabled = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches(),
                        onClick = {
                            showGoogleSignIn.value = false
                            emailLogInClicked()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                        ),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.log_in).uppercase(), color = Color.Black
                        )
                    }
                    if (emailSent) {
                        Text(
                            modifier = Modifier
                                .padding(top = 48.dp)
                                .padding(horizontal = 32.dp),
                            text = stringResource(R.string.email_msg_sent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val activity = LocalContext.current as Activity
    viewModel.googleSignInInit()
    LoginScreen(email = viewModel.email.value,
        onEmailChange = { viewModel.email.value = it },
        emailSent = viewModel.emailSent.value,
        emailLogInClicked = viewModel::emailLogInClicked,
        googleSignInClicked = { viewModel.googleSignInClicked(activity) })
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen("abcd@ef.gh", {}, true, {}, {})
}
