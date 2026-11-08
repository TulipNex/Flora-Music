import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

decl_target = """    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()"""
decl_replacement = """    val lyricsOtherFontColor by viewModel.lyricsOtherFontColor.collectAsState()
    val lyricsContextLines by viewModel.lyricsContextLines.collectAsState()
    val lyricsFadeEffect by viewModel.lyricsFadeEffect.collectAsState()"""
content = content.replace(decl_target, decl_replacement)

# We need to change the `alphaVal` calculation logic in BOTH `FullScreenLyricsView` and `SynchronizedLyricsLayout`.
# Here's the current calculation:
'''
                                val alphaVal = when (lyricsActivePosition) {
                                    "TOP" -> {
                                        if (distance < 0) 0f
                                        else when (distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    "BOTTOM" -> {
                                        if (distance > 0) 0f
                                        else when (-distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    else -> {
                                        when (kotlin.math.abs(distance)) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                }
'''

# We want to replace it with a function or logic that uses `lyricsContextLines` and `lyricsFadeEffect`.
# Let's just create a helper block inside the itemsIndexed, or replace it directly.
new_alpha_logic = """
                                val absDist = kotlin.math.abs(distance)
                                val showLine = if (lyricsContextLines > 5) true else absDist <= lyricsContextLines
                                
                                val rawAlpha = when (lyricsActivePosition) {
                                    "TOP" -> if (distance < 0) 0f else if (!showLine) 0f else {
                                        if (lyricsFadeEffect == "NONE") 1.0f
                                        else if (distance == 0) 1.0f
                                        else {
                                            val factor = distance.toFloat() / (lyricsContextLines.coerceAtLeast(1) + 1)
                                            if (lyricsFadeEffect == "SHARP") 1.0f - factor
                                            else 1.0f - (factor * 0.5f) // SMOOTH
                                        }
                                    }
                                    "BOTTOM" -> if (distance > 0) 0f else if (!showLine) 0f else {
                                        if (lyricsFadeEffect == "NONE") 1.0f
                                        else if (-distance == 0) 1.0f
                                        else {
                                            val factor = (-distance).toFloat() / (lyricsContextLines.coerceAtLeast(1) + 1)
                                            if (lyricsFadeEffect == "SHARP") 1.0f - factor
                                            else 1.0f - (factor * 0.5f)
                                        }
                                    }
                                    else -> if (!showLine) 0f else {
                                        if (lyricsFadeEffect == "NONE") 1.0f
                                        else if (absDist == 0) 1.0f
                                        else {
                                            val limit = if (lyricsContextLines > 5) 5 else lyricsContextLines
                                            val factor = absDist.toFloat() / (limit + 1)
                                            if (lyricsFadeEffect == "SHARP") (1.0f - factor).coerceAtLeast(0.1f)
                                            else (1.0f - (factor * 0.5f)).coerceAtLeast(0.1f)
                                        }
                                    }
                                }
                                val alphaVal = rawAlpha.coerceIn(0f, 1f)
"""

old_alpha_logic = """                                val alphaVal = when (lyricsActivePosition) {
                                    "TOP" -> {
                                        if (distance < 0) 0f
                                        else when (distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    "BOTTOM" -> {
                                        if (distance > 0) 0f
                                        else when (-distance) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                    else -> {
                                        when (kotlin.math.abs(distance)) {
                                            0 -> 1.0f
                                            1 -> 0.75f
                                            2 -> 0.5f
                                            3 -> 0.25f
                                            else -> 0.0f
                                        }
                                    }
                                }"""

content = content.replace(old_alpha_logic, new_alpha_logic.strip())

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
