package com.example.nutritrack.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle register state changes
    LaunchedEffect(registerState) {
        when (registerState) {
            is UiState.Success -> {
                viewModel.resetRegisterState()
                onRegisterSuccess()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (registerState as UiState.Error).message,
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
                text = "Buat Akun Baru",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Daftar untuk memulai gaya hidup sehatmu",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = LightGreen.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = DarkGreen,
                unfocusedIndicatorColor = TextGray.copy(alpha = 0.5f),
                cursorColor = DarkGreen,
                focusedLeadingIconColor = DarkGreen,
                unfocusedLeadingIconColor = TextGray,
            )

            // Username Field
            TextField(
                value = authState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
                isError = authState.usernameError != null,
                supportingText = {
                    authState.usernameError?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                colors = textFieldColors
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
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Register Button
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = MaterialTheme.shapes.medium,
                enabled = registerState !is UiState.Loading
            ) {
                if (registerState is UiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Daftar", fontSize = 18.sp)
                }
            }

            // Login Link
            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah punya akun?", color = TextGray)
                TextButton(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkGreen)
                ) {
                    Text("Masuk di sini", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    NutriTrackTheme {
        RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {})
    }
}
