import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

bad_pattern = r"""                                        } else \{
                                            Text\(
                                                text = line\.text,
                                                fontFamily = customFontFamily,
                                                fontSize = \(if \(line\.isBackground\)
                                    \} baseFontSize \* 0\.75f else baseFontSize\)\.sp,
                                        fontWeight = FontWeight\.Bold,
                                        color = colorState,
                                        textAlign = align,
                                        lineHeight = \(baseFontSize \* lyricsLineHeightMultiplier\)\.sp,
                                        modifier = Modifier
                                            \.fillMaxWidth\(\)
                                            \.clickable \{ 
                                                 isAutoScrollLocked = true
                                                onSeekTo\(line\.timestampMs\)
                                            \}
                                    \)"""

# Since my regex might be too rigid with spaces, let's just use string replace for the broken part
content = content.replace("                                    } baseFontSize * 0.75f else baseFontSize).sp,\n                                        fontWeight = FontWeight.Bold,\n                                        color = colorState,\n                                        textAlign = align,\n                                        lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,\n                                        modifier = Modifier\n                                            .fillMaxWidth()\n                                            .clickable { \n                                                 isAutoScrollLocked = true\n                                                onSeekTo(line.timestampMs)\n                                            }\n                                    )", "")
content = content.replace("                                    } baseFontSize * 0.75f else baseFontSize).sp,\n                                        fontWeight = FontWeight.Bold,\n                                        color = colorState,\n                                        textAlign = align,\n                                        lineHeight = (baseFontSize * lyricsLineHeightMultiplier).sp,\n                                        modifier = Modifier\n                                            .fillMaxWidth()\n                                            .clickable { \n                                                 isAutoScrollLocked = true\n                                                onSeekTo(line.timestampMs)\n                                            }\n                                    )", "")

# Wait, there are multiple spaces maybe. Let's just do a regex replace on the broken part.
broken_regex = r'(\s*)\}\s*baseFontSize \* 0\.75f else baseFontSize\)\.sp,[\s\S]*?\)\s*'
content = re.sub(broken_regex, '\n', content)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
