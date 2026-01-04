package com.example.nutritrack.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            name = user.name
            age = user.age.toString()
            height = user.height.toString()
            weight = user.weight.toString()
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Profile updated successfully")
                viewModel.resetUpdateState()
                onNavigateBack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((updateState as UiState.Error).message)
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.user == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen
                    )
                }

                uiState.errorMessage != null && uiState.user == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadUserProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Profile Picture Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(DarkGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = DarkGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { /* TODO: Add photo picker */ }) {
                                    Text("Change Photo", color = DarkGreen)
                                }
                            }
                        }

                        // Name Field
                        Text(
                            "Name",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field (Read-only)
                        Text(
                            "Email",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.user?.email ?: "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = TextGray.copy(alpha = 0.3f),
                                disabledTextColor = TextGray
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Age Field
                        Text(
                            "Age",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = age,
                            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your age") },
                            leadingIcon = {
                                Icon(Icons.Default.Cake, contentDescription = null)
                            },
                            suffix = { Text("years") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Height Field
                        Text(
                            "Height",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = height,
                            onValueChange = { if (it.all { char -> char.isDigit() }) height = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your height") },
                            leadingIcon = {
                                Icon(Icons.Default.Height, contentDescription = null)
                            },
                            suffix = { Text("cm") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Weight Field
                        Text(
                            "Weight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your weight") },
                            leadingIcon = {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                            },
                            suffix = { Text("kg") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Save Button
                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    name = name,
                                    age = age.toIntOrNull() ?: 0,
                                    height = height.toIntOrNull() ?: 0,
                                    weight = weight.toDoubleOrNull() ?: 0.0
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = updateState !is UiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkGreen
                            )
                        ) {
                            if (updateState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    "Save Changes",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
