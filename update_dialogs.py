import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """                if (showActiveColorDialog) {
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
                                    },"""

replacement1 = """                if (showActiveColorDialog) {
                    var hue by remember { mutableFloatStateOf(0f) }
                    var saturation by remember { mutableFloatStateOf(1f) }
                    var brightness by remember { mutableFloatStateOf(1f) }
                    var alpha by remember { mutableFloatStateOf(1f) }
                    
                    LaunchedEffect(customActiveColorInput) {
                        try {
                            val colorStr = if (customActiveColorInput.length == 7) customActiveColorInput.replace("#", "#FF") else customActiveColorInput
                            val colorInt = android.graphics.Color.parseColor(colorStr)
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                            alpha = android.graphics.Color.alpha(colorInt) / 255f
                        } catch (e: Exception) {}
                    }
                    
                    AlertDialog(
                        onDismissRequest = { showActiveColorDialog = false },
                        title = { Text("Warna Baris Aktif", color = Color.White) },
                        text = {
                            Column {
                                Text("Hue (Warna)", color = Color.White, fontSize = 12.sp)
                                HueSlider(
                                    hue = hue,
                                    onHueChange = { newHue ->
                                        hue = newHue
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customActiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Saturasi", color = Color.White, fontSize = 12.sp)
                                SaturationSlider(
                                    hue = hue,
                                    saturation = saturation,
                                    value = brightness,
                                    onSaturationChange = { newSat ->
                                        saturation = newSat
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customActiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Kecerahan", color = Color.White, fontSize = 12.sp)
                                ValueSlider(
                                    hue = hue,
                                    saturation = saturation,
                                    value = brightness,
                                    onValueChange = { newVal ->
                                        brightness = newVal
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customActiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Transparansi (Alpha)", color = Color.White, fontSize = 12.sp)
                                val previewColorBase = try { Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))) } catch(e:Exception) { Color.White }
                                AlphaSlider(
                                    color = previewColorBase,
                                    alpha = alpha,
                                    onAlphaChange = { newAlpha ->
                                        alpha = newAlpha
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customActiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Atau masukkan kode HEX manual", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customActiveColorInput,
                                    onValueChange = { customActiveColorInput = it.uppercase() },"""

target2 = """                if (showInactiveColorDialog) {
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
                                    },"""

replacement2 = """                if (showInactiveColorDialog) {
                    var hue by remember { mutableFloatStateOf(0f) }
                    var saturation by remember { mutableFloatStateOf(1f) }
                    var brightness by remember { mutableFloatStateOf(1f) }
                    var alpha by remember { mutableFloatStateOf(1f) }
                    
                    LaunchedEffect(customInactiveColorInput) {
                        try {
                            val colorStr = if (customInactiveColorInput.length == 7) customInactiveColorInput.replace("#", "#FF") else customInactiveColorInput
                            val colorInt = android.graphics.Color.parseColor(colorStr)
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                            alpha = android.graphics.Color.alpha(colorInt) / 255f
                        } catch (e: Exception) {}
                    }

                    AlertDialog(
                        onDismissRequest = { showInactiveColorDialog = false },
                        title = { Text("Warna Baris Tidak Aktif", color = Color.White) },
                        text = {
                            Column {
                                Text("Hue (Warna)", color = Color.White, fontSize = 12.sp)
                                HueSlider(
                                    hue = hue,
                                    onHueChange = { newHue ->
                                        hue = newHue
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customInactiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Saturasi", color = Color.White, fontSize = 12.sp)
                                SaturationSlider(
                                    hue = hue,
                                    saturation = saturation,
                                    value = brightness,
                                    onSaturationChange = { newSat ->
                                        saturation = newSat
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customInactiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Kecerahan", color = Color.White, fontSize = 12.sp)
                                ValueSlider(
                                    hue = hue,
                                    saturation = saturation,
                                    value = brightness,
                                    onValueChange = { newVal ->
                                        brightness = newVal
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customInactiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Text("Transparansi (Alpha)", color = Color.White, fontSize = 12.sp)
                                val previewColorBase = try { Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))) } catch(e:Exception) { Color.White }
                                AlphaSlider(
                                    color = previewColorBase,
                                    alpha = alpha,
                                    onAlphaChange = { newAlpha ->
                                        alpha = newAlpha
                                        val colorInt = android.graphics.Color.HSVToColor((alpha * 255).toInt(), floatArrayOf(hue, saturation, brightness))
                                        customInactiveColorInput = String.format("#%08X", colorInt)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Atau masukkan kode HEX manual", color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customInactiveColorInput,
                                    onValueChange = { customInactiveColorInput = it.uppercase() },"""

if target1 in content:
    content = content.replace(target1, replacement1)
    if target2 in content:
        content = content.replace(target2, replacement2)
        with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
            f.write(content)
        print("Replaced successfully")
    else:
        print("Target 2 not found")
else:
    print("Target 1 not found")
