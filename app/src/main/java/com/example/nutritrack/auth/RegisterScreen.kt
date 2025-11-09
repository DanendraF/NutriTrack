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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.LightGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header ---
            Text(
                text = "Buat Akun Baru",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Mulai perjalanan sehatmu bersama kami",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // --- Name Field ---
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon") },
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

            // --- Email Field ---
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
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

            // --- Password Field ---
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
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

            // --- Register Button ---
            Button(
                onClick = {
                    // TODO: Logika registrasi
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Daftar", fontSize = 18.sp)
            }

            // --- Login Link ---
            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah punya akun?", color = TextGray)
                TextButton(
                    onClick = { navController.navigate("login") },
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
        RegisterScreen(navController = rememberNavController())
    }
}
