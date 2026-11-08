import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {
            // Preview Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .background(CardSlate, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val align = when (lyricsAlignment) {
                    "LEFT" -> TextAlign.Left
                    "RIGHT" -> TextAlign.Right
                    else -> TextAlign.Center
                }

                val colAlign = when (lyricsAlignment) {
                    "LEFT" -> Alignment.Start
                    "RIGHT" -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }

                val activeColorStr = if (lyricsUseDominantColor) "#FFFFFF" else lyricsActiveFontColor
                val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor

                val activeColor = try { Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { Color.White }
                val otherColor = try { Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { TextSecondary }

                val arr = when(lyricsActivePosition) {
                    "TOP" -> Arrangement.Top
                    "BOTTOM" -> Arrangement.Bottom
                    else -> Arrangement.Center
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = arr,
                    horizontalAlignment = colAlign
                ) {
                    Text(
                        text = "Baris lirik sebelumnya...", 
                        color = otherColor, 
                        fontSize = lyricsOtherFontSize.sp, 
                        textAlign = align, 
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ini adalah lirik yang sedang aktif", 
                        color = activeColor, 
                        fontSize = lyricsActiveFontSize.sp, 
                        textAlign = align, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Baris lirik selanjutnya...", 
                        color = otherColor, 
                        fontSize = lyricsOtherFontSize.sp, 
                        textAlign = align, 
                        fontWeight = FontWeight.Normal
                    )
                }
            }"""

replacement = """        // Preview Window - Fixed at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(CardSlate, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            val align = when (lyricsAlignment) {
                "LEFT" -> TextAlign.Left
                "RIGHT" -> TextAlign.Right
                else -> TextAlign.Center
            }

            val colAlign = when (lyricsAlignment) {
                "LEFT" -> Alignment.Start
                "RIGHT" -> Alignment.End
                else -> Alignment.CenterHorizontally
            }

            val activeColorStr = if (lyricsUseDominantColor) "#FFFFFF" else lyricsActiveFontColor
            val otherColorStr = if (lyricsUseDominantColor) "#888888" else lyricsOtherFontColor

            val activeColor = try { Color(android.graphics.Color.parseColor(activeColorStr)) } catch(e: Exception) { Color.White }
            val otherColor = try { Color(android.graphics.Color.parseColor(otherColorStr)) } catch(e: Exception) { TextSecondary }

            val arr = when(lyricsActivePosition) {
                "TOP" -> Arrangement.Top
                "BOTTOM" -> Arrangement.Bottom
                else -> Arrangement.Center
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = arr,
                horizontalAlignment = colAlign
            ) {
                if (lyricsActivePosition != "TOP") {
                    Text(
                        text = "Baris lirik sebelumnya...", 
                        color = otherColor, 
                        fontSize = lyricsOtherFontSize.sp, 
                        textAlign = align, 
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Ini adalah lirik yang sedang aktif", 
                    color = activeColor, 
                    fontSize = lyricsActiveFontSize.sp, 
                    textAlign = align, 
                    fontWeight = FontWeight.Bold
                )
                if (lyricsActivePosition != "BOTTOM") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Baris lirik selanjutnya...", 
                        color = otherColor, 
                        fontSize = lyricsOtherFontSize.sp, 
                        textAlign = align, 
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
