package com.example.nutritrack.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.ui.theme.NutriTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(viewModel: OnboardingViewModel, onNavigateNext: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val genders = listOf("Laki-laki", "Perempuan")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Isi Profil Kesehatanmu") }) },
        bottomBar = {
            Button(
                onClick = onNavigateNext,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = uiState.name.isNotBlank() && uiState.age.isNotBlank() && uiState.height.isNotBlank() && uiState.weight.isNotBlank()
            ) {
                Text("Lanjut ➜")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp)) // Spacer at the top

            // Name Input
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Nama Panggilan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Gender Selection
            Text("Jenis Kelamin", style = MaterialTheme.typography.bodyLarge)
            Row(Modifier.fillMaxWidth()) {
                genders.forEach { gender ->
                    Row(
                        Modifier
                            .weight(1f)
                            .selectable(
                                selected = (uiState.gender == gender),
                                onClick = { viewModel.updateGender(gender) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.gender == gender),
                            onClick = null // Recommended for accessibility
                        )
                        Text(
                            text = gender,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Age Input
            OutlinedTextField(
                value = uiState.age,
                onValueChange = viewModel::updateAge,
                label = { Text("Usia (Tahun)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Height Input
            OutlinedTextField(
                value = uiState.height,
                onValueChange = viewModel::updateHeight,
                label = { Text("Tinggi Badan (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Weight Input
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = viewModel::updateWeight,
                label = { Text("Berat Badan (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun HealthProfileScreenPreview() {
    NutriTrackTheme {
        GenderAgeScreen(viewModel(), {}, {}, 2, 5)
    }
}
