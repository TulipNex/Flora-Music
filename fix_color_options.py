import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """            if (!lyricsUseDominantColor) {
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
                
                Text("Baris Tidak Aktif", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
            }"""

replacement = """            if (!lyricsUseDominantColor) {
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
                                    customActiveColorInput = lyricsActiveFontColor
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
                                    customInactiveColorInput = lyricsOtherFontColor
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
                    AlertDialog(
                        onDismissRequest = { showActiveColorDialog = false },
                        title = { Text("Warna Baris Aktif", color = Color.White) },
                        text = {
                            Column {
                                Text("Masukkan kode warna HEX (misal: #FF0000 untuk merah)", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = customActiveColorInput,
                                    onValueChange = { customActiveColorInput = it.uppercase() },
                                    label = { Text("Kode HEX", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = CardSlate
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                // Preview
                                val previewColor = try { Color(android.graphics.Color.parseColor(customActiveColorInput)) } catch (e: Exception) { Color.Transparent }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(previewColor, RoundedCornerShape(8.dp))
                                        .background(if (previewColor == Color.Transparent) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                ) {
                                    if (previewColor == Color.Transparent) {
                                        Text("Kode warna tidak valid", color = Color.White, modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    try {
                                        android.graphics.Color.parseColor(customActiveColorInput)
                                        viewModel.setLyricsActiveFontColor(customActiveColorInput)
                                        showActiveColorDialog = false
                                    } catch (e: Exception) {
                                        // Invalid color
                                    }
                                }
                            ) {
                                Text("Terapkan", color = EmeraldGreen)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showActiveColorDialog = false }) {
                                Text("Batal", color = TextSecondary)
                            }
                        },
                        containerColor = CardSlate
                    )
                }
                
                if (showInactiveColorDialog) {
                    AlertDialog(
                        onDismissRequest = { showInactiveColorDialog = false },
                        title = { Text("Warna Baris Tidak Aktif", color = Color.White) },
                        text = {
                            Column {
                                Text("Masukkan kode warna HEX (misal: #FF0000 untuk merah)", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = customInactiveColorInput,
                                    onValueChange = { customInactiveColorInput = it.uppercase() },
                                    label = { Text("Kode HEX", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = CardSlate
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                // Preview
                                val previewColor = try { Color(android.graphics.Color.parseColor(customInactiveColorInput)) } catch (e: Exception) { Color.Transparent }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(previewColor, RoundedCornerShape(8.dp))
                                        .background(if (previewColor == Color.Transparent) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                ) {
                                    if (previewColor == Color.Transparent) {
                                        Text("Kode warna tidak valid", color = Color.White, modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    try {
                                        android.graphics.Color.parseColor(customInactiveColorInput)
                                        viewModel.setLyricsOtherFontColor(customInactiveColorInput)
                                        showInactiveColorDialog = false
                                    } catch (e: Exception) {
                                        // Invalid color
                                    }
                                }
                            ) {
                                Text("Terapkan", color = EmeraldGreen)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showInactiveColorDialog = false }) {
                                Text("Batal", color = TextSecondary)
                            }
                        },
                        containerColor = CardSlate
                    )
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
