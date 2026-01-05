package com.example.nutritrack.presentation.meal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritrack.domain.model.MealType
import com.example.nutritrack.domain.model.UiState
import com.example.nutritrack.presentation.auth.FirebaseAuthViewModel
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoodSearch: () -> Unit = {},
    viewModel: MealViewModel = koinViewModel(),
    authViewModel: FirebaseAuthViewModel = koinViewModel()
) {
    val uiState by viewModel.addMealState.collectAsStateWithLifecycle()
    val saveMealState by viewModel.saveMealState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save result
    LaunchedEffect(saveMealState) {
        when (saveMealState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Makanan berhasil disimpan!")
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
                title = {
                    Text(
                        "Tambah Makanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = TextBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Search Food Button
                    OutlinedButton(
                        onClick = onNavigateToFoodSearch,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkGreen
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cari dari Database", fontSize = 14.sp)
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Food Name Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Informasi Makanan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        OutlinedTextField(
                            value = uiState.foodName,
                            onValueChange = { viewModel.updateFoodName(it) },
                            label = { Text("Nama Makanan", fontSize = 14.sp) },
                            placeholder = { Text("Contoh: Nasi Goreng", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = DarkGreen
                                )
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
                    }

                    // Meal Type Selection
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Waktu Makan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MealType.values().forEach { mealType ->
                                FilterChip(
                                    selected = uiState.mealType == mealType,
                                    onClick = { viewModel.updateMealType(mealType) },
                                    label = {
                                        Text(
                                            mealType.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DarkGreen,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF5F5F5)
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Serving & Quantity Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Porsi & Jumlah",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.servingSize,
                                onValueChange = { viewModel.updateServingSize(it) },
                                label = { Text("Ukuran", fontSize = 14.sp) },
                                placeholder = { Text("100g", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkGreen,
                                    focusedLabelColor = DarkGreen,
                                    cursorColor = DarkGreen
                                )
                            )

                            OutlinedTextField(
                                value = uiState.quantity.toString(),
                                onValueChange = {
                                    it.toFloatOrNull()?.let { qty -> viewModel.updateQuantity(qty) }
                                },
                                label = { Text("Jumlah", fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkGreen,
                                    focusedLabelColor = DarkGreen,
                                    cursorColor = DarkGreen
                                )
                            )
                        }

                        // Quantity Stepper
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
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
                                    Icon(
                                        Icons.Default.RemoveCircle,
                                        contentDescription = "Kurangi",
                                        tint = DarkGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Text(
                                    text = String.format("%.1f", uiState.quantity),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.updateQuantity(uiState.quantity + 0.5f) }
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = "Tambah",
                                        tint = DarkGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Nutrition Info Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Informasi Nutrisi (Per Porsi)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        // Calories
                        OutlinedTextField(
                            value = if (uiState.caloriesPerServing == 0) "" else uiState.caloriesPerServing.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { cal -> viewModel.updateCaloriesPerServing(cal) }
                                    ?: if (it.isEmpty()) viewModel.updateCaloriesPerServing(0) else {}
                            },
                            label = { Text("Kalori", fontSize = 14.sp) },
                            placeholder = { Text("kcal", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = DarkGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen,
                                cursorColor = DarkGreen
                            )
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
                                label = { Text("Protein", fontSize = 14.sp) },
                                placeholder = { Text("g", fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkGreen,
                                    focusedLabelColor = DarkGreen,
                                    cursorColor = DarkGreen
                                )
                            )

                            // Carbs
                            OutlinedTextField(
                                value = if (uiState.carbsPerServing == 0) "" else uiState.carbsPerServing.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let { carbs -> viewModel.updateCarbsPerServing(carbs) }
                                        ?: if (it.isEmpty()) viewModel.updateCarbsPerServing(0) else {}
                                },
                                label = { Text("Karbo", fontSize = 14.sp) },
                                placeholder = { Text("g", fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkGreen,
                                    focusedLabelColor = DarkGreen,
                                    cursorColor = DarkGreen
                                )
                            )

                            // Fat
                            OutlinedTextField(
                                value = if (uiState.fatPerServing == 0) "" else uiState.fatPerServing.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let { fat -> viewModel.updateFatPerServing(fat) }
                                        ?: if (it.isEmpty()) viewModel.updateFatPerServing(0) else {}
                                },
                                label = { Text("Lemak", fontSize = 14.sp) },
                                placeholder = { Text("g", fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
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
                }
            }

            // Total Nutrition Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkGreen.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Total Nutrisi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )

                    NutritionRow("Kalori", "${uiState.totalCalories} kcal", Icons.Default.LocalFireDepartment)
                    NutritionRow("Protein", "${uiState.totalProtein}g")
                    NutritionRow("Karbohidrat", "${uiState.totalCarbs}g")
                    NutritionRow("Lemak", "${uiState.totalFat}g")
                }
            }

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
                    .padding(16.dp),
                enabled = saveMealState !is UiState.Loading && uiState.foodName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (saveMealState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Makanan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Text(
                text = label,
                fontSize = 14.sp,
                color = TextBlack
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )
    }
}
