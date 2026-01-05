package com.example.nutritrack.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(viewModel: OnboardingViewModel, onNavigateNext: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val genders = listOf("Laki-laki", "Perempuan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil Kesehatan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextBlack
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Content Card
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
                    Text(
                        "Isi data dirimu untuk perhitungan nutrisi yang akurat",
                        fontSize = 14.sp,
                        color = TextGray,
                        lineHeight = 20.sp
                    )

                    // Name Input
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Nama Panggilan", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
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

                    // Gender Selection
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Jenis Kelamin",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextBlack
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            genders.forEach { gender ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = (uiState.gender == gender),
                                            onClick = { viewModel.updateGender(gender) },
                                            role = Role.RadioButton
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (uiState.gender == gender)
                                            DarkGreen.copy(alpha = 0.1f)
                                        else
                                            Color(0xFFF5F5F5)
                                    ),
                                    border = if (uiState.gender == gender)
                                        CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(DarkGreen),
                                            width = 2.dp
                                        )
                                    else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        RadioButton(
                                            selected = (uiState.gender == gender),
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = DarkGreen
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = gender,
                                            fontSize = 14.sp,
                                            fontWeight = if (uiState.gender == gender)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal,
                                            color = if (uiState.gender == gender)
                                                DarkGreen
                                            else
                                                TextBlack
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Age Input
                    OutlinedTextField(
                        value = uiState.age,
                        onValueChange = viewModel::updateAge,
                        label = { Text("Usia", fontSize = 14.sp) },
                        placeholder = { Text("Tahun", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Cake,
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

                    // Height & Weight Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Height Input
                        OutlinedTextField(
                            value = uiState.height,
                            onValueChange = viewModel::updateHeight,
                            label = { Text("Tinggi", fontSize = 14.sp) },
                            placeholder = { Text("cm", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Height,
                                    contentDescription = null,
                                    tint = DarkGreen
                                )
                            },
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

                        // Weight Input
                        OutlinedTextField(
                            value = uiState.weight,
                            onValueChange = viewModel::updateWeight,
                            label = { Text("Berat", fontSize = 14.sp) },
                            placeholder = { Text("kg", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = DarkGreen
                                )
                            },
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

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Button
            Button(
                onClick = onNavigateNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = uiState.name.isNotBlank() &&
                        uiState.age.isNotBlank() &&
                        uiState.height.isNotBlank() &&
                        uiState.weight.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    disabledContainerColor = Color.Gray
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    "Lanjutkan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HealthProfileScreenPreview() {
    NutriTrackTheme {
        HealthProfileScreen(viewModel(), {})
    }
}
