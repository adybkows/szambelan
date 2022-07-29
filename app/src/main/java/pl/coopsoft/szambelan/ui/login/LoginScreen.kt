package pl.coopsoft.szambelan.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.coopsoft.szambelan.R
import pl.coopsoft.szambelan.ui.theme.MainTheme

@Composable
fun LoginScreen(
    uniqueIdentifier: String,
    onUniqueIdentifierChange: (String) -> Unit,
    randomizeClicked: () -> Unit,
    logInClicked: () -> Unit
) {
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
                Text(
                    text = stringResource(R.string.unique_identifier)
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    value = uniqueIdentifier,
                    onValueChange = { value ->
                        if (value != uniqueIdentifier) {
                            onUniqueIdentifierChange(value)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrect = false
                    )
                )
                Button(
                    onClick = { randomizeClicked() },
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.randomize).uppercase()
                    )
                }
                Button(
                    onClick = { logInClicked() },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green
                    ),
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.log_in).uppercase(),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen("abcd", {}, {}, {})
}
