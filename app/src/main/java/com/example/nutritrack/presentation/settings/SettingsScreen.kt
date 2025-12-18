package com.example.nutritrack.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // App Preferences Section
            SectionTitle("Preferences")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        description = "Receive meal reminders and tips",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )

                    Divider()

                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        description = "Enable dark theme",
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Section
            SectionTitle("Account")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        description = "Update your personal information",
                        onClick = { /* TODO: Navigate to edit profile */ }
                    )

                    Divider()

                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        description = "Update your password",
                        onClick = { /* TODO: Navigate to change password */ }
                    )

                    Divider()

                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        description = "Permanently delete your account",
                        onClick = { /* TODO: Show delete confirmation */ },
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data & Privacy Section
            SectionTitle("Data & Privacy")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        description = "Download your nutrition data",
                        onClick = { /* TODO: Export data */ }
                    )

                    Divider()

                    SettingsItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear Cache",
                        description = "Free up storage space",
                        onClick = { /* TODO: Clear cache */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Support Section
            SectionTitle("Support")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help & FAQ",
                        description = "Get help and find answers",
                        onClick = { /* TODO: Navigate to help */ }
                    )

                    Divider()

                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        description = "Share your thoughts with us",
                        onClick = { /* TODO: Open feedback form */ }
                    )

                    Divider()

                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        description = "Version 1.0.0",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Version Footer
            Text(
                "NutriTrack v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = DarkGreen
                )
            },
            title = {
                Text(
                    "About NutriTrack",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "NutriTrack helps you track your nutrition goals and maintain a healthy lifestyle.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Developed with Kotlin & Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
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
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.error else DarkGreen
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else TextBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextGray
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CardBackground,
                checkedTrackColor = DarkGreen,
                uncheckedThumbColor = CardBackground,
                uncheckedTrackColor = TextGray
            )
        )
    }
}
