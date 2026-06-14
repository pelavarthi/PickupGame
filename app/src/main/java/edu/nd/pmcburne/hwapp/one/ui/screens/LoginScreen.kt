package edu.nd.pmcburne.hwapp.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import edu.nd.pmcburne.hwapp.one.R
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreenDark
import edu.nd.pmcburne.hwapp.one.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.web_client_id)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    color = PickupGreenDark,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = PickupGreen,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\u26BD", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PickupGame",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Find your next game nearby",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "EMAIL",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("you@email.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PASSWORD",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isSignUpMode) authViewModel.signUp(email, password)
                    else authViewModel.signIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PickupGreen),
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "Sign Up" else "Log In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    if (webClientId.isBlank()) {
                        authViewModel.signInWithGoogle("")
                        return@OutlinedButton
                    }
                    scope.launch {
                        try {
                            val option = GetGoogleIdOption.Builder()
                                .setServerClientId(webClientId)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(option)
                                .build()
                            val response = CredentialManager.create(context)
                                .getCredential(context, request)
                            val cred = response.credential
                            if (cred is CustomCredential &&
                                cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val token = GoogleIdTokenCredential
                                    .createFrom(cred.data).idToken
                                authViewModel.signInWithGoogle(token)
                            }
                        } catch (_: GetCredentialCancellationException) {
                            // user dismissed the sheet — no error needed
                        } catch (e: NoCredentialException) {
                            authViewModel.setError(
                                "NoCredentialException: ${e.message ?: "no Google accounts available, or app's SHA-1 not registered in Firebase"}"
                            )
                        } catch (e: Exception) {
                            authViewModel.setError(
                                "${e.javaClass.simpleName}: ${e.message ?: "unknown error"}"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? " else "No account? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (isSignUpMode) "Log in" else "Sign up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PickupGreen,
                    modifier = Modifier.clickable {
                        isSignUpMode = !isSignUpMode
                        authViewModel.clearError()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (authState.errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                action = {
                    Text(
                        text = "Dismiss",
                        color = Color.White,
                        modifier = Modifier.clickable { authViewModel.clearError() }
                    )
                }
            ) {
                Text(
                    text = authState.errorMessage ?: "",
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
