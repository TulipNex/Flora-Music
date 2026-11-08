import os

content = """package com.example.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSlate
import com.example.ui.theme.DividerSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = EmeraldGreen,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit
) {
    val lyricsAlignment by viewModel.lyricsAlignment.collectAsState()
    val lyricsActivePosition by viewModel.lyricsActivePosition.collectAsState()
    val lyricsActiveFontSize by viewModel.lyricsActiveFontSize.collectAsState()
    val lyricsOtherFontSize by viewModel.lyricsOtherFontSize.collectAsState()
    val lyricsSameFontSize by viewModel.lyricsSameFontSize.collectAsState()
    val lyricsUseDominantColor by viewModel.lyricsUseDominantColor.collectAsState()
    val lyricsActiveFontColor by viewModel.lyricsActiveFontColor.collectAsState()
    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()
    val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()
    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()
    val lyricsMargin by viewModel.lyricsMargin.collectAsState()
    
    var showAlignmentDialog by remember { mutableStateOf(false) }
    var showPositionDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan Lirik", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {
            // ==========================================
            // SECTION 1: TATA LETAK & POSISI
            // ==========================================
            SettingsSectionTitle("Tata Letak & Posisi")
            
            // Perataan Lirik
            val alignmentLabels = mapOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
            ListItem(
                headlineContent = { Text("Perataan Lirik", color = Color.White) },
                supportingContent = { Text(alignmentLabels[lyricsAlignment] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showAlignmentDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            if (showAlignmentDialog) {
                AlertDialog(
                    onDismissRequest = { showAlignmentDialog = false },
                    title = { Text("Perataan Lirik", color = Color.White) },
                    text = {
                        Column {
                            alignmentLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setLyricsAlignment(value)
                                            showAlignmentDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = lyricsAlignment == value,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, color = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAlignmentDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }

            // Posisi Lirik Aktif
            val positionLabels = mapOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
            ListItem(
                headlineContent = { Text("Posisi Lirik Aktif", color = Color.White) },
                supportingContent = { Text(positionLabels[lyricsActivePosition] ?: "Tengah", color = TextSecondary) },
                trailingContent = {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Pilih", tint = Color.White)
                },
                modifier = Modifier.clickable { showPositionDialog = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            if (showPositionDialog) {
                AlertDialog(
                    onDismissRequest = { showPositionDialog = false },
                    title = { Text("Posisi Lirik Aktif", color = Color.White) },
                    text = {
                        Column {
                            positionLabels.forEach { (value, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setLyricsActivePosition(value)
                                            showPositionDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = lyricsActivePosition == value,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen, unselectedColor = Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, color = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPositionDialog = false }) {
                            Text("Batal", color = EmeraldGreen)
                        }
                    },
                    containerColor = CardSlate
                )
            }
            
            // Margin Horizontal
            Text(
                text = "Margin Horizontal: ${lyricsMargin.roundToInt()}dp", 
                color = Color.White, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Slider(
                value = lyricsMargin,
                onValueChange = { viewModel.setLyricsMargin(it) },
                valueRange = 0f..64f,
                steps = 15,
                colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Batas Baris Lirik
            Text(
                text = if (lyricsContextLines > 5) "Batas Baris Lirik: Tampilkan Semua" else "Batas Baris Lirik: $lyricsContextLines baris sebelum/sesudah", 
                color = Color.White, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Slider(
                value = lyricsContextLines.toFloat(),
                onValueChange = { viewModel.setLyricsContextLines(it.roundToInt()) },
                valueRange = 1f..6f,
                steps = 4,
                colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // ==========================================
            // SECTION 2: TIPOGRAFI
            // ==========================================
            SettingsSectionTitle("Tipografi")

            ListItem(
                headlineContent = { Text("Samakan Ukuran Font", color = Color.White) },
                supportingContent = { Text("Gunakan ukuran yang sama untuk semua lirik", color = TextSecondary) },
                trailingContent = {
                    Switch(
                        checked = lyricsSameFontSize,
                        onCheckedChange = { viewModel.setLyricsSameFontSize(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f))
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Text(
                text = if (lyricsSameFontSize) "Ukuran Font (${lyricsActiveFontSize.roundToInt()}sp)" else "Ukuran Font Lirik Aktif (${lyricsActiveFontSize.roundToInt()}sp)",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Slider(
                value = lyricsActiveFontSize,
                onValueChange = { viewModel.setLyricsActiveFontSize(it) },
                valueRange = 12f..36f,
                steps = 23,
                colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (!lyricsSameFontSize) {
                Text(
                    text = "Ukuran Font Lirik Biasa (${lyricsOtherFontSize.roundToInt()}sp)",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Slider(
                    value = lyricsOtherFontSize,
                    onValueChange = { viewModel.setLyricsOtherFontSize(it) },
                    valueRange = 12f..36f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen, inactiveTrackColor = CardSlate),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            // ==========================================
            // SECTION 3: WARNA & EFEK
            // ==========================================
            SettingsSectionTitle("Warna & Efek")

            ListItem(
                headlineContent = { Text("Warna Dominan Album Art", color = Color.White) },
                supportingContent = { Text("Sesuaikan warna lirik dengan sampul album", color = TextSecondary) },
                trailingContent = {
                    Switch(
                        checked = lyricsUseDominantColor,
                        onCheckedChange = { viewModel.setLyricsUseDominantColor(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f))
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            if (!lyricsUseDominantColor) {
                val colorOptions = listOf(
                    "#FFFFFF" to "Putih",
                    "#10B981" to "Hijau",
                    "#EF4444" to "Merah",
                    "#3B82F6" to "Biru",
                    "#F59E0B" to "Kuning",
                    "#8B5CF6" to "Ungu",
                    "#888888" to "Abu-abu"
                )
                
                Text("Warna Lirik Aktif", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, _) = colorOptions[index]
                        val isSelected = lyricsActiveFontColor == hex
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(parsedColor, RoundedCornerShape(24.dp))
                                .clickable { viewModel.setLyricsActiveFontColor(hex) }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (parsedColor.luminance() > 0.5) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Warna Lirik Biasa", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, _) = colorOptions[index]
                        val isSelected = lyricsOtherFontColor == hex
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(parsedColor, RoundedCornerShape(24.dp))
                                .clickable { viewModel.setLyricsOtherFontColor(hex) }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier)
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (parsedColor.luminance() > 0.5) Color.Black else Color.White, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Efek Memudar (Fade Effect)
            Text(
                text = "Efek Memudar",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SMOOTH" to "Halus", "SHARP" to "Tajam", "NONE" to "Mati").forEach { (value, label) ->
                    val isSelected = lyricsFadeEffect == value
                    Button(
                        onClick = { viewModel.setLyricsFadeEffect(value) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) EmeraldGreen else CardSlate,
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
"""

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

