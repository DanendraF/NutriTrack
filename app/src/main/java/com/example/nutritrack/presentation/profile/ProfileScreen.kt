package com.example.nutritrack.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            is UiState.Success -> {
                viewModel.resetLogoutState()
                onNavigateToLogin()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (logoutState as UiState.Error).message
                )
                viewModel.resetLogoutState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen
                    )
                }

                uiState.errorMessage != null -> {
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkGreen
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                uiState.user != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Profile Header
                        ProfileHeader(
                            name = uiState.user!!.name,
                            email = uiState.user!!.email
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Personal Info Section
                        SectionTitle("Personal Information")
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Gender",
                                    value = uiState.user!!.gender.replaceFirstChar { it.uppercase() }
                                )
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    icon = Icons.Default.Cake,
                                    label = "Age",
                                    value = "${uiState.user!!.age} years"
                                )
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    icon = Icons.Default.Height,
                                    label = "Height",
                                    value = "${uiState.user!!.height} cm"
                                )
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    icon = Icons.Default.FitnessCenter,
                                    label = "Weight",
                                    value = "${uiState.user!!.weight} kg"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nutrition Goals Section
                        SectionTitle("Nutrition Goals")
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow(
                                    icon = Icons.Default.DirectionsRun,
                                    label = "Activity Level",
                                    value = uiState.user!!.activityLevel.displayName
                                )
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    icon = Icons.Default.TrackChanges,
                                    label = "Goal",
                                    value = uiState.user!!.nutritionGoal.displayName
                                )
                                Divider(modifier = Modifier.padding(vertical = 12.dp))
                                InfoRow(
                                    icon = Icons.Default.LocalFireDepartment,
                                    label = "Daily Calories",
                                    value = "${uiState.user!!.targetCalories} kcal"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Macros Section
                        SectionTitle("Daily Macros Target")
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CardBackground
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroItem(
                                    label = "Protein",
                                    value = "${uiState.user!!.targetMacros.protein.toInt()}g",
                                    color = DarkGreen
                                )
                                MacroItem(
                                    label = "Carbs",
                                    value = "${uiState.user!!.targetMacros.carbs.toInt()}g",
                                    color = OrangeIndicator
                                )
                                MacroItem(
                                    label = "Fat",
                                    value = "${uiState.user!!.targetMacros.fat.toInt()}g",
                                    color = Purple40
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Logout Button
                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Logout",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGreen
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = DarkGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CardBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                color = CardBackground.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextBlack
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = DarkGreen
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextBlack
            )
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )
    }
}
