package com.example.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSlate
import com.example.ui.theme.DividerSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongLimitSettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit
) {
    val historyLimit by viewModel.historyLimit.collectAsState()
    val mostPlayedLimit by viewModel.mostPlayedLimit.collectAsState()
    val recentlyAddedValue by viewModel.recentlyAddedValue.collectAsState()
    val recentlyAddedUnit by viewModel.recentlyAddedUnit.collectAsState()

    var showHistoryDialog by remember { mutableStateOf(false) }
    var historyInputText by remember { mutableStateOf("") }

    var showMostPlayedDialog by remember { mutableStateOf(false) }
    var mostPlayedInputText by remember { mutableStateOf("") }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Batas Lagu Riwayat", color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "Tentukan batas maksimal lagu di tab riwayat (masukkan 0 untuk tanpa batas):",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = historyInputText,
                        onValueChange = { historyInputText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Lagu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF2E3832)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = historyInputText.toIntOrNull() ?: 50
                        viewModel.setHistoryLimit(num)
                        showHistoryDialog = false
                    }
                ) {
                    Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = CardSlate
        )
    }

    if (showMostPlayedDialog) {
        AlertDialog(
            onDismissRequest = { showMostPlayedDialog = false },
            title = { Text("Batas Lagu Teratas", color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "Tentukan batas maksimal lagu di tab teratas (masukkan 0 untuk tanpa batas):",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mostPlayedInputText,
                        onValueChange = { mostPlayedInputText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Lagu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF2E3832)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = mostPlayedInputText.toIntOrNull() ?: 50
                        viewModel.setMostPlayedLimit(num)
                        showMostPlayedDialog = false
                    }
                ) {
                    Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMostPlayedDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = CardSlate
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batas Lagu", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Group 1: Batas Jumlah Lagu
            Text(
                text = "Batas Jumlah Lagu",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Batas Riwayat
            ListItem(
                modifier = Modifier.clickable {
                    historyInputText = historyLimit.toString()
                    showHistoryDialog = true
                },
                headlineContent = { Text("Batas Lagu Riwayat", color = Color.White) },
                supportingContent = { Text("Jumlah maksimal lagu yang tampil di riwayat", color = TextSecondary) },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (historyLimit <= 0) "Tanpa Batas" else "$historyLimit Lagu",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Batas Teratas
            ListItem(
                modifier = Modifier.clickable {
                    mostPlayedInputText = mostPlayedLimit.toString()
                    showMostPlayedDialog = true
                },
                headlineContent = { Text("Batas Lagu Teratas", color = Color.White) },
                supportingContent = { Text("Jumlah maksimal lagu terpopuler yang tampil", color = TextSecondary) },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (mostPlayedLimit <= 0) "Tanpa Batas" else "$mostPlayedLimit Lagu",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Group 2: Filter Tab Terbaru
            Text(
                text = "Filter Tab Terbaru",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSlate)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Rentang Waktu Lagu Terbaru",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Lagu yang ditambahkan dalam rentang waktu ini akan tampil di tab Terbaru:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Stepper Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Jumlah Rentang:",
                        color = Color.White,
                        fontSize = 13.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (recentlyAddedValue > 1) {
                                    viewModel.setRecentlyAddedRange(recentlyAddedValue - 1, recentlyAddedUnit)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF262E2A), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E2421),
                            border = BorderStroke(1.dp, Color(0xFF2E3832))
                        ) {
                            Text(
                                text = "$recentlyAddedValue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.setRecentlyAddedRange(recentlyAddedValue + 1, recentlyAddedUnit)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF262E2A), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Pill Control for Unit: [ Hari | Minggu | Bulan | Tahun ]
                Text(
                    text = "Satuan Waktu:",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val units = listOf("Hari", "Minggu", "Bulan", "Tahun")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF191F1C))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    units.forEach { unit ->
                        val isSelected = recentlyAddedUnit.equals(unit, ignoreCase = true)
                        Surface(
                            onClick = { viewModel.setRecentlyAddedRange(recentlyAddedValue, unit) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldGreen else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = unit,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status summary badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aktif: Menampilkan lagu dalam $recentlyAddedValue $recentlyAddedUnit terakhir",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
