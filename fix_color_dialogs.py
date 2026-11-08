import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                if (showActiveColorDialog) {
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
                }"""

replacement = """                if (showActiveColorDialog) {
                    var hue by remember { mutableFloatStateOf(0f) }
                    
                    LaunchedEffect(Unit) {
                        try {
                            val colorInt = android.graphics.Color.parseColor(customActiveColorInput)
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            hue = hsv[0]
                        } catch (e: Exception) {}
                    }
                    
                    AlertDialog(
                        onDismissRequest = { showActiveColorDialog = false },
                        title = { Text("Warna Baris Aktif", color = Color.White) },
                        text = {
                            Column {
                                HueSlider(
                                    hue = hue,
                                    onHueChange = { newHue ->
                                        hue = newHue
                                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                                        customActiveColorInput = String.format("#%06X", (0xFFFFFF and colorInt))
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Atau masukkan kode HEX manual", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customActiveColorInput,
                                    onValueChange = { 
                                        customActiveColorInput = it.uppercase()
                                        try {
                                            val colorInt = android.graphics.Color.parseColor(customActiveColorInput)
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(colorInt, hsv)
                                            hue = hsv[0]
                                        } catch (e: Exception) {}
                                    },
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
                    var hue by remember { mutableFloatStateOf(0f) }
                    
                    LaunchedEffect(Unit) {
                        try {
                            val colorInt = android.graphics.Color.parseColor(customInactiveColorInput)
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            hue = hsv[0]
                        } catch (e: Exception) {}
                    }

                    AlertDialog(
                        onDismissRequest = { showInactiveColorDialog = false },
                        title = { Text("Warna Baris Tidak Aktif", color = Color.White) },
                        text = {
                            Column {
                                HueSlider(
                                    hue = hue,
                                    onHueChange = { newHue ->
                                        hue = newHue
                                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                                        customInactiveColorInput = String.format("#%06X", (0xFFFFFF and colorInt))
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Atau masukkan kode HEX manual", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customInactiveColorInput,
                                    onValueChange = { 
                                        customInactiveColorInput = it.uppercase()
                                        try {
                                            val colorInt = android.graphics.Color.parseColor(customInactiveColorInput)
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(colorInt, hsv)
                                            hue = hsv[0]
                                        } catch (e: Exception) {}
                                    },
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
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
