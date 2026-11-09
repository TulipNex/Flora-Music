package com.example.presentation

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSlate
import com.example.ui.theme.DividerSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun <T> SegmentedPills(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2421))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            Surface(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) EmeraldGreen else Color.Transparent,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun LyricsColorPickerDialog(
    title: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var mode by remember { mutableStateOf("round") } // "round" or "sliders"
    var customHexInput by remember { mutableStateOf(initialColorHex) }

    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var alpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(initialColorHex) {
        try {
            val colorStr = if (initialColorHex.length == 7) initialColorHex.replace("#", "#FF") else initialColorHex
            val colorInt = android.graphics.Color.parseColor(colorStr)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(colorInt, hsv)
            hue = hsv[0]
            saturation = hsv[1]
            brightness = hsv[2]
            alpha = android.graphics.Color.alpha(colorInt) / 255f
        } catch (e: Exception) {}
    }

    val updateHexFromHsv = {
        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
        customHexInput = String.format("#%08X", colorInt)
    }

    val quickPresets = listOf(
        "#10B981", "#34D399", "#38BDF8", "#F43F5E",
        "#F59E0B", "#A855F7", "#FFFFFF", "#9CA3AF"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode Switcher: [ Roda Bulat | Slider Linear ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2421))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { mode = "round" },
                        shape = RoundedCornerShape(6.dp),
                        color = if (mode == "round") EmeraldGreen else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                "Roda Bulat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mode == "round") Color.Black else TextSecondary
                            )
                        }
                    }
                    Surface(
                        onClick = { mode = "sliders" },
                        shape = RoundedCornerShape(6.dp),
                        color = if (mode == "sliders") EmeraldGreen else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                "Slider Linear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mode == "sliders") Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (mode == "round") {
                    // MODE 1: Round Color Picker (Wheel)
                    RoundColorPicker(
                        hue = hue,
                        saturation = saturation,
                        value = brightness,
                        onColorChange = { newHue, newSat ->
                            hue = newHue
                            saturation = newSat
                            updateHexFromHsv()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Kecerahan: ${(brightness * 100).toInt()}%",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    ValueSlider(
                        hue = hue,
                        saturation = saturation,
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            updateHexFromHsv()
                        }
                    )
                } else {
                    // MODE 2: Linear Sliders
                    Text("Hue (Warna)", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                    HueSlider(
                        hue = hue,
                        onHueChange = {
                            hue = it
                            updateHexFromHsv()
                        }
                    )
                    Text("Saturasi", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                    SaturationSlider(
                        hue = hue,
                        saturation = saturation,
                        value = brightness,
                        onSaturationChange = {
                            saturation = it
                            updateHexFromHsv()
                        }
                    )
                    Text("Kecerahan", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                    ValueSlider(
                        hue = hue,
                        saturation = saturation,
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            updateHexFromHsv()
                        }
                    )
                    Text("Transparansi (Alpha)", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                    val previewColorBase = try {
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness)))
                    } catch (e: Exception) { Color.White }
                    AlphaSlider(
                        color = previewColorBase,
                        alpha = alpha,
                        onAlphaChange = {
                            alpha = it
                            updateHexFromHsv()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets
                Text("Warna Cepat:", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPresets.forEach { hex ->
                        val presetColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .border(1.dp, Color(0xFF2E3832), CircleShape)
                                .clickable {
                                    customHexInput = hex
                                    try {
                                        val colorStr = if (hex.length == 7) hex.replace("#", "#FF") else hex
                                        val colorInt = android.graphics.Color.parseColor(colorStr)
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(colorInt, hsv)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        brightness = hsv[2]
                                        alpha = android.graphics.Color.alpha(colorInt) / 255f
                                    } catch (e: Exception) {}
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual HEX text field
                OutlinedTextField(
                    value = customHexInput,
                    onValueChange = {
                        customHexInput = it.uppercase()
                        try {
                            val colorStr = if (it.length == 7) it.replace("#", "#FF") else it
                            val colorInt = android.graphics.Color.parseColor(colorStr)
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                            alpha = android.graphics.Color.alpha(colorInt) / 255f
                        } catch (e: Exception) {}
                    },
                    label = { Text("Kode HEX Manual", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = CardSlate
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preview box
                val previewColor = try { Color(android.graphics.Color.parseColor(customHexInput)) } catch (e: Exception) { Color.Transparent }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(1.dp, Color(0xFF2E3832), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewColor == Color.Transparent) {
                        Text("Kode warna tidak valid", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        android.graphics.Color.parseColor(customHexInput)
                        onConfirm(customHexInput)
                    } catch (e: Exception) {}
                }
            ) {
                Text("Terapkan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = CardSlate
    )
}

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
    val lyricsItemSpacing by viewModel.lyricsItemSpacing.collectAsState()
    val lyricsLineHeightMultiplier by viewModel.lyricsLineHeightMultiplier.collectAsState()
    val lyricsFontFamily by viewModel.lyricsFontFamily.collectAsState()
    val lyricsCustomFontUri by viewModel.lyricsCustomFontUri.collectAsState()

    var showFontDialog by remember { mutableStateOf(false) }
    
    val customFontFamilyContext = androidx.compose.ui.platform.LocalContext.current
    val customFontFamily = remember(lyricsFontFamily, lyricsCustomFontUri, customFontFamilyContext) {
        if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(lyricsCustomFontUri)
                val pfd = customFontFamilyContext.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val typeface = android.graphics.Typeface.Builder(pfd.fileDescriptor).build()
                    pfd.close()
                    if (typeface != null) {
                        androidx.compose.ui.text.font.FontFamily(typeface)
                    } else null
                } else null
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            } ?: androidx.compose.ui.text.font.FontFamily.Default
        } else {
            when (lyricsFontFamily) {
                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                else -> androidx.compose.ui.text.font.FontFamily.Default
            }
        }
    }
    
    val context = LocalContext.current
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.setLyricsCustomFontUri(uri.toString())
            viewModel.setLyricsFontFamily("CUSTOM")
        }
    }
    
    var showActiveColorDialog by remember { mutableStateOf(false) }
    var showInactiveColorDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pengaturan Lirik", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        }

        val align = when (lyricsAlignment) {
            "LEFT" -> TextAlign.Left
            "RIGHT" -> TextAlign.Right
            else -> TextAlign.Center
        }
        val currentSong by viewModel.playerController.currentSong.collectAsState()
        var songArtBytes by remember(currentSong?.path) { mutableStateOf<ByteArray?>(null) }
        LaunchedEffect(currentSong?.path) {
            currentSong?.path?.let {
                songArtBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.utils.MusicScanner.getArtworkBytes(it)
                }
            }
        }
        val artBitmap = remember(songArtBytes) {
            songArtBytes?.let {
                try {
                    android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                } catch (e: Exception) {
                    null
                }
            }
        }
        var artDominantColor by remember { mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
        LaunchedEffect(artBitmap) {
            if (artBitmap != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    try {
                        val palette = androidx.palette.graphics.Palette.from(artBitmap).generate()
                        val vibrant = palette.vibrantSwatch
                        val lightVibrant = palette.lightVibrantSwatch
                        val dominant = palette.dominantSwatch
                        val colorInt = lightVibrant?.rgb ?: vibrant?.rgb ?: dominant?.rgb ?: android.graphics.Color.WHITE
                        artDominantColor = androidx.compose.ui.graphics.Color(colorInt)
                    } catch (e: Exception) {
                        artDominantColor = androidx.compose.ui.graphics.Color.White
                    }
                }
            } else {
                artDominantColor = null
            }
        }

        val activeColor = if (lyricsUseDominantColor) {
            artDominantColor ?: Color(android.graphics.Color.parseColor("#10B981"))
        } else {
            try { Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch(e: Exception) { Color.White }
        }
        
        val otherColor = if (lyricsUseDominantColor) {
            (artDominantColor ?: Color(android.graphics.Color.parseColor("#10B981"))).copy(alpha = 0.5f)
        } else {
            try { Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch(e: Exception) { Color.Gray }
        }
        val staticFontSizeOther = if (lyricsSameFontSize) lyricsActiveFontSize.sp else lyricsOtherFontSize.sp
        
        val factor = 1f / (lyricsContextLines.coerceAtLeast(1) + 1)
        val otherAlpha = if (lyricsFadeEffect == "NONE") 1.0f else if (lyricsFadeEffect == "SHARP") (1.0f - factor).coerceAtLeast(0.1f) else (1.0f - (factor * 0.5f)).coerceAtLeast(0.1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSlate.copy(alpha = 0.3f))
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = lyricsMargin.dp), verticalArrangement = Arrangement.spacedBy(lyricsItemSpacing.dp)) {
                if ((lyricsActivePosition == "CENTER" || lyricsActivePosition == "BOTTOM") && lyricsContextLines > 0) {
                    Text(
                        text = "Baris lirik sebelumnya...",
                        fontFamily = customFontFamily,
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),
                        textAlign = align,
                        fontWeight = FontWeight.Bold,
                        lineHeight = staticFontSizeOther * lyricsLineHeightMultiplier,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = "Baris lirik yang sedang diputar",
                    fontFamily = customFontFamily,
                    fontSize = lyricsActiveFontSize.sp,
                    color = activeColor,
                    textAlign = align,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (lyricsActiveFontSize * lyricsLineHeightMultiplier).sp,
                    modifier = Modifier.fillMaxWidth()
                )
                if ((lyricsActivePosition == "CENTER" || lyricsActivePosition == "TOP") && lyricsContextLines > 0) {
                    Text(
                        text = "Baris lirik selanjutnya...",
                        fontFamily = customFontFamily,
                        fontSize = staticFontSizeOther,
                        color = otherColor.copy(alpha = otherAlpha),
                        textAlign = align,
                        fontWeight = FontWeight.Bold,
                        lineHeight = staticFontSizeOther * lyricsLineHeightMultiplier,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
            SettingsSectionTitle("Tata Letak dan Posisi")
            
            // Perataan Lirik
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Perataan Lirik",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val alignmentLabels = listOf("LEFT" to "Kiri", "CENTER" to "Tengah", "RIGHT" to "Kanan")
                SegmentedPills(
                    options = alignmentLabels,
                    selected = lyricsAlignment,
                    onSelect = { viewModel.setLyricsAlignment(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Posisi Lirik Aktif
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Posisi Baris Aktif",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val positionLabels = listOf("TOP" to "Atas", "CENTER" to "Tengah", "BOTTOM" to "Bawah")
                SegmentedPills(
                    options = positionLabels,
                    selected = lyricsActivePosition,
                    onSelect = { viewModel.setLyricsActivePosition(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Jarak Antar Baris (Item Spacing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Jarak Antar Baris", color = Color.White, fontSize = 14.sp)
                Text(text = "${lyricsItemSpacing.roundToInt()} dp", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            MinimalSlider(
                value = lyricsItemSpacing,
                onValueChange = { viewModel.setLyricsItemSpacing(it) },
                valueRange = 8f..64f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Margin Horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Margin Horizontal", color = Color.White, fontSize = 14.sp)
                Text(text = "${lyricsMargin.roundToInt()} dp", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            MinimalSlider(
                value = lyricsMargin,
                onValueChange = { viewModel.setLyricsMargin(it) },
                valueRange = 0f..64f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Batas Baris Lirik
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Batas Baris Lirik", color = Color.White, fontSize = 14.sp)
                Text(
                    text = if (lyricsContextLines > 5) "Semua Baris" else "$lyricsContextLines Baris",
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            MinimalSlider(
                value = lyricsContextLines.toFloat(),
                onValueChange = { viewModel.setLyricsContextLines(it.roundToInt()) },
                valueRange = 1f..6f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // ==========================================
            // SECTION 1.5: FONT LIRIK
            // ==========================================
            SettingsSectionTitle("Font")
            
            val fontLabels = mapOf(
                "DEFAULT" to "Default",
                "SERIF" to "Serif",
                "SANS_SERIF" to "Sans Serif",
                "MONOSPACE" to "Monospace",
                "CURSIVE" to "Cursive",
                "CUSTOM" to "Custom"
            )
            
            ListItem(
                headlineContent = { Text("Font Lirik", color = Color.White) },
                supportingContent = { Text(if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom" else fontLabels[lyricsFontFamily] ?: "Default", color = TextSecondary) },
                trailingContent = {
                    Box {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .clickable { showFontDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lyricsFontFamily == "CUSTOM" && lyricsCustomFontUri.isNotEmpty()) "Custom" else fontLabels[lyricsFontFamily] ?: "Default",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Pilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        if (showFontDialog) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopStart,
                                onDismissRequest = { showFontDialog = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .background(CardSlate, RoundedCornerShape(8.dp))
                                        .padding(3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        fontLabels.forEach { (value, label) ->
                                            val isSelected = lyricsFontFamily == value
                                            val previewFontFamily = when (value) {
                                                "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                                                "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                                "SANS_SERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                                                "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                                                "CUSTOM" -> customFontFamily
                                                else -> androidx.compose.ui.text.font.FontFamily.Default
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { 
                                                        if (value == "CUSTOM") {
                                                            fontPickerLauncher.launch(arrayOf("*/*"))
                                                        } else {
                                                            viewModel.setLyricsFontFamily(value)
                                                        }
                                                        showFontDialog = false
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontFamily = previewFontFamily,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // ==========================================
            // SECTION 2: TIPOGRAFI
            // ==========================================
            SettingsSectionTitle("Tipografi")
            
            // Jarak Antar Kata (Line Height)
            val formattedLineHeight = String.format(java.util.Locale.US, "%.1f", lyricsLineHeightMultiplier)
            Text(
                text = "Jarak Baris: ${formattedLineHeight}x", 
                color = Color.White, 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            MinimalSlider(
                value = lyricsLineHeightMultiplier,
                onValueChange = { viewModel.setLyricsLineHeightMultiplier(it) },
                valueRange = 1.0f..2.5f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Ukuran Font Sama", color = Color.White) },
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
                text = if (lyricsSameFontSize) "Ukuran Font: ${lyricsActiveFontSize.roundToInt()}sp" else "Baris Aktif: ${lyricsActiveFontSize.roundToInt()}sp",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            MinimalSlider(
                value = lyricsActiveFontSize,
                onValueChange = { viewModel.setLyricsActiveFontSize(it) },
                valueRange = 12f..36f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (!lyricsSameFontSize) {
                Text(
                    text = "Baris Tidak Aktif: ${lyricsOtherFontSize.roundToInt()}sp",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                MinimalSlider(
                value = lyricsOtherFontSize,
                onValueChange = { viewModel.setLyricsOtherFontSize(it) },
                valueRange = 12f..36f,
                activeColor = EmeraldGreen,
                inactiveColor = CardSlate,
                modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            HorizontalDivider(color = DividerSlate, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            // ==========================================
            // SECTION 3: WARNA & EFEK
            // ==========================================
            SettingsSectionTitle("Warna dan Efek")

            ListItem(
                headlineContent = { Text("Warna Dominan Album Art", color = Color.White) },
                supportingContent = { Text("Sesuaikan warna lirik dengan warna dominan sampul album", color = TextSecondary) },
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
                
                Text("Baris Aktif", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, _) = colorOptions[index]
                        val isSelected = lyricsActiveFontColor == hex
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
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
                    
                    item {
                        // Custom Color Option
                        val isSelected = colorOptions.none { it.first == lyricsActiveFontColor }
                        val activeColor = try { Color(android.graphics.Color.parseColor(lyricsActiveFontColor)) } catch (e: Exception) { Color.White }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isSelected) activeColor else CardSlate, RoundedCornerShape(24.dp))
                                .clickable { 
                                    showActiveColorDialog = true 
                                }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (activeColor.luminance() > 0.5) Color.Black else Color.White)
                            } else {
                                Icon(Icons.Default.Add, contentDescription = "Warna Kustom", tint = Color.White)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Baris Tidak Aktif", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorOptions.size) { index ->
                        val (hex, _) = colorOptions[index]
                        val isSelected = lyricsOtherFontColor == hex
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
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
                    
                    item {
                        // Custom Color Option
                        val isSelected = colorOptions.none { it.first == lyricsOtherFontColor }
                        val inactiveColor = try { Color(android.graphics.Color.parseColor(lyricsOtherFontColor)) } catch (e: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isSelected) inactiveColor else CardSlate, RoundedCornerShape(24.dp))
                                .clickable { 
                                    showInactiveColorDialog = true 
                                }
                                .then(if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (inactiveColor.luminance() > 0.5) Color.Black else Color.White)
                            } else {
                                Icon(Icons.Default.Add, contentDescription = "Warna Kustom", tint = Color.White)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (showActiveColorDialog) {
                    LyricsColorPickerDialog(
                        title = "Warna Baris Aktif",
                        initialColorHex = lyricsActiveFontColor,
                        onDismiss = { showActiveColorDialog = false },
                        onConfirm = {
                            viewModel.setLyricsActiveFontColor(it)
                            showActiveColorDialog = false
                        }
                    )
                }
                
                if (showInactiveColorDialog) {
                    LyricsColorPickerDialog(
                        title = "Warna Baris Tidak Aktif",
                        initialColorHex = lyricsOtherFontColor,
                        onDismiss = { showInactiveColorDialog = false },
                        onConfirm = {
                            viewModel.setLyricsOtherFontColor(it)
                            showInactiveColorDialog = false
                        }
                    )
                }
            }

            // Efek Memudar (Fade Effect)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "Efek Memudar (Fade Effect)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val fadeLabels = listOf("SMOOTH" to "Halus", "SHARP" to "Tajam", "NONE" to "Mati")
                SegmentedPills(
                    options = fadeLabels,
                    selected = lyricsFadeEffect,
                    onSelect = { viewModel.setLyricsFadeEffect(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
