package com.example.nutritrack.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.LightGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleOnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Auto-fill email from Firebase
    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.email?.let { email ->
            viewModel.updateEmail(email)
        }
    }

    // Handle success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onOnboardingComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F9F9),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Header Card with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkGreen, LightGreen)
                        )
                    )
                    .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackToLogin,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Login"
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Welcome Icon
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Complete Your Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tell us about yourself to get personalized nutrition recommendations",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Personal Information Section
                SectionCard(
                    title = "Personal Information",
                    icon = Icons.Default.Person
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Name
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen,
                                cursorColor = DarkGreen
                            )
                        )

                        // Email (read-only)
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = {},
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Gender Selection
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkGreen
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GenderCard(
                                gender = "Male",
                                icon = Icons.Default.Person,
                                isSelected = uiState.gender == "Male",
                                onClick = { viewModel.updateGender("Male") },
                                modifier = Modifier.weight(1f)
                            )
                            GenderCard(
                                gender = "Female",
                                icon = Icons.Default.Person,
                                isSelected = uiState.gender == "Female",
                                onClick = { viewModel.updateGender("Female") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Age
                        OutlinedTextField(
                            value = uiState.age,
                            onValueChange = { viewModel.updateAge(it) },
                            label = { Text("Age") },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            },
                            suffix = { Text("years", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen,
                                cursorColor = DarkGreen
                            )
                        )
                    }
                }

                // Physical Measurements Section
                SectionCard(
                    title = "Physical Measurements",
                    icon = Icons.Default.Settings
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Height
                        OutlinedTextField(
                            value = uiState.height,
                            onValueChange = { viewModel.updateHeight(it) },
                            label = { Text("Height") },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null)
                            },
                            suffix = { Text("cm", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen,
                                cursorColor = DarkGreen
                            )
                        )

                        // Weight
                        OutlinedTextField(
                            value = uiState.weight,
                            onValueChange = { viewModel.updateWeight(it) },
                            label = { Text("Weight") },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            },
                            suffix = { Text("kg", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen,
                                cursorColor = DarkGreen
                            )
                        )
                    }
                }

                // Activity Level Section
                SectionCard(
                    title = "Activity Level",
                    icon = Icons.Default.FavoriteBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            Triple("Sedentary", "Little or no exercise", Icons.Default.Person),
                            Triple("Lightly Active", "Exercise 1-3 days/week", Icons.Default.Face),
                            Triple("Moderately Active", "Exercise 3-5 days/week", Icons.Default.Star),
                            Triple("Very Active", "Exercise 6-7 days/week", Icons.Default.Favorite)
                        ).forEach { (level, desc, icon) ->
                            ActivityLevelCard(
                                level = level,
                                description = desc,
                                icon = icon,
                                isSelected = uiState.activityLevel == level,
                                onClick = { viewModel.updateActivityLevel(level) }
                            )
                        }
                    }
                }

                // Nutrition Goal Section
                SectionCard(
                    title = "Your Nutrition Goal",
                    icon = Icons.Default.FavoriteBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            Triple("Lose Weight", "Calorie deficit for weight loss", Icons.Default.FavoriteBorder),
                            Triple("Maintain Weight", "Balanced calories", Icons.Default.CheckCircle),
                            Triple("Gain Weight", "Calorie surplus for muscle gain", Icons.Default.Favorite)
                        ).forEach { (goal, desc, icon) ->
                            GoalCard(
                                goal = goal,
                                description = desc,
                                icon = icon,
                                isSelected = uiState.goal == goal,
                                onClick = { viewModel.updateGoal(goal) }
                            )
                        }
                    }
                }

                // Error message
                AnimatedVisibility(
                    visible = uiState.saveError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.saveError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        firebaseAuth.currentUser?.uid?.let { userId ->
                            viewModel.saveUserData(userId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        disabledContainerColor = DarkGreen.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Complete Setup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }
            content()
        }
    }
}

@Composable
private fun GenderCard(
    gender: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkGreen else Color(0xFFF5F5F5)
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else DarkGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = gender,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun ActivityLevelCard(
    level: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DarkGreen, LightGreen)),
            width = 2.dp
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) DarkGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isSelected) DarkGreen else Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) DarkGreen.copy(alpha = 0.8f) else Color.Gray
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkGreen.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DarkGreen, LightGreen)),
            width = 2.dp
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) DarkGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isSelected) DarkGreen else Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) DarkGreen.copy(alpha = 0.8f) else Color.Gray
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

