package com.example.nutritrack.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme

/**
 * A composable that creates a row of segmented buttons.
 *
 * @param modifier The modifier to be applied to the row of buttons.
 * @param items The list of strings to be displayed in the buttons.
 * @param selectedIndex The index of the currently selected item.
 * @param shape The base shape for the buttons, defaults to `ButtonDefaults.shape`.
 * @param onItemClick A callback that is invoked when a button is clicked.
 */
@Composable
fun SegmentedButton(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    shape: Shape = ButtonDefaults.shape, // Menggunakan shape default dari MaterialTheme
    onItemClick: (Int) -> Unit
) {
    Row(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            // Menentukan bentuk tombol berdasarkan posisinya (awal, tengah, atau akhir)
            val buttonShape = when (index) {
                0 -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                items.lastIndex -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                else -> AbsoluteCutCornerShape(0.dp) // Sudut tajam untuk bagian tengah
            }

            OutlinedButton(
                onClick = { onItemClick(index) },
                shape = buttonShape,
                // Menggunakan zIndex (via offset) agar tombol terpilih tampak di atas tetangganya
                modifier = Modifier
                    .offset(x = (-1 * index).dp)
                    .clip(buttonShape), // Clip untuk memastikan shape diterapkan dengan benar
                colors = if (isSelected) {
                    // Warna untuk tombol yang sedang terpilih
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkGreen,
                        contentColor = Color.White
                    )
                } else {
                    // Warna untuk tombol yang tidak terpilih
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = DarkGreen
                    )
                },
                border = BorderStroke(1.dp, DarkGreen)
            ) {
                Text(item, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}


@Preview(showBackground = true, name = "Segmented Button Preview")
@Composable
private fun SegmentedButtonPreview() {
    NutriTrackTheme {
        SegmentedButton(
            items = listOf("Berat Badan", "Tinggi Badan"),
            selectedIndex = 0,
            onItemClick = {}
        )
    }
}
