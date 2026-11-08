import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target_1 = """                                        .graphicsLayer {
                                            alpha = animatedAlpha
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                        }"""

replacement_1 = """                                        .graphicsLayer {
                                            alpha = animatedAlpha
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                            transformOrigin = when (lyricsAlignment) {
                                                "LEFT" -> androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                                "RIGHT" -> androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                                                else -> androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                            }
                                        }"""

content = content.replace(target_1, replacement_1)

target_2 = """                            .graphicsLayer {
                                alpha = animatedAlpha
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }"""

replacement_2 = """                            .graphicsLayer {
                                alpha = animatedAlpha
                                scaleX = animatedScale
                                scaleY = animatedScale
                                transformOrigin = when (lyricsAlignment) {
                                    "LEFT" -> androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                    "RIGHT" -> androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                                    else -> androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                }
                            }"""

content = content.replace(target_2, replacement_2)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

