package com.example.nutritrack.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.LightGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                viewModel.resetLoginState()
                onLoginSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (loginState as UiState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Selamat Datang!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Masuk untuk melanjutkan petualangan gizimu",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // Email Field
            TextField(
                value = authState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                isError = authState.emailError != null,
                supportingText = {
                    authState.emailError?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightGreen.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = DarkGreen,
                    unfocusedIndicatorColor = TextGray.copy(alpha = 0.5f),
                    cursorColor = DarkGreen,
                    focusedLeadingIconColor = DarkGreen,
                    unfocusedLeadingIconColor = TextGray,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            TextField(
                value = authState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                isError = authState.passwordError != null,
                supportingText = {
                    authState.passwordError?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightGreen.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = DarkGreen,
                    unfocusedIndicatorColor = TextGray.copy(alpha = 0.5f),
                    cursorColor = DarkGreen,
                    focusedLeadingIconColor = DarkGreen,
                    unfocusedLeadingIconColor = TextGray,
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Button
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = MaterialTheme.shapes.medium,
                enabled = loginState !is UiState.Loading
            ) {
                if (loginState is UiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Masuk", fontSize = 18.sp)
                }
            }

            // Register Link
            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Belum punya akun?", color = TextGray)
                TextButton(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkGreen)
                ) {
                    Text("Daftar di sini", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NutriTrackTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {})
    }
}
