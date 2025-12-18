package com.example.nutritrack.presentation.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.ui.theme.DarkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onNavigateBack: () -> Unit,
    viewModel: MealViewModel = hiltViewModel(),
    authViewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.addMealState.collectAsStateWithLifecycle()
    val saveMealState by viewModel.saveMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save result
    LaunchedEffect(saveMealState) {
        when (saveMealState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Meal saved successfully!")
                viewModel.resetSaveState()
                viewModel.resetForm()
                onNavigateBack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    (saveMealState as UiState.Error).message
                )
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Meal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Food Name
            OutlinedTextField(
                value = uiState.foodName,
                onValueChange = { viewModel.updateFoodName(it) },
                label = { Text("Food Name") },
                leadingIcon = {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Meal Type Selection
            Text(
                text = "Meal Type",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealType.values().forEach { mealType ->
                    FilterChip(
                        selected = uiState.mealType == mealType,
                        onClick = { viewModel.updateMealType(mealType) },
                        label = { Text(mealType.displayName) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Divider()

            // Serving Size & Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.servingSize,
                    onValueChange = { viewModel.updateServingSize(it) },
                    label = { Text("Serving Size") },
                    placeholder = { Text("e.g., 100g, 1 cup") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.quantity.toString(),
                    onValueChange = {
                        it.toFloatOrNull()?.let { qty -> viewModel.updateQuantity(qty) }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Quantity Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (uiState.quantity > 0.5f) {
                            viewModel.updateQuantity(uiState.quantity - 0.5f)
                        }
                    }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = String.format("%.1f", uiState.quantity),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                IconButton(
                    onClick = { viewModel.updateQuantity(uiState.quantity + 0.5f) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            Divider()

            // Nutrition Info (Per Serving)
            Text(
                text = "Nutrition per Serving",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            // Calories
            OutlinedTextField(
                value = if (uiState.caloriesPerServing == 0) "" else uiState.caloriesPerServing.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { cal -> viewModel.updateCaloriesPerServing(cal) }
                        ?: if (it.isEmpty()) viewModel.updateCaloriesPerServing(0) else {}
                },
                label = { Text("Calories (kcal)") },
                leadingIcon = {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Protein
                OutlinedTextField(
                    value = if (uiState.proteinPerServing == 0) "" else uiState.proteinPerServing.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { protein -> viewModel.updateProteinPerServing(protein) }
                            ?: if (it.isEmpty()) viewModel.updateProteinPerServing(0) else {}
                    },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Carbs
                OutlinedTextField(
                    value = if (uiState.carbsPerServing == 0) "" else uiState.carbsPerServing.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { carbs -> viewModel.updateCarbsPerServing(carbs) }
                            ?: if (it.isEmpty()) viewModel.updateCarbsPerServing(0) else {}
                    },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Fat
                OutlinedTextField(
                    value = if (uiState.fatPerServing == 0) "" else uiState.fatPerServing.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { fat -> viewModel.updateFatPerServing(fat) }
                            ?: if (it.isEmpty()) viewModel.updateFatPerServing(0) else {}
                    },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider()

            // Total Nutrition Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total Nutrition",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    NutritionRow("Calories", "${uiState.totalCalories} kcal", Icons.Default.LocalFireDepartment)
                    NutritionRow("Protein", "${uiState.totalProtein}g")
                    NutritionRow("Carbs", "${uiState.totalCarbs}g")
                    NutritionRow("Fat", "${uiState.totalFat}g")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    val userId = authViewModel.getCurrentUserId()
                    if (userId != null) {
                        viewModel.saveMeal(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = saveMealState !is UiState.Loading && uiState.foodName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                if (saveMealState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Meal", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = label, fontSize = 16.sp)
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )
    }
}
