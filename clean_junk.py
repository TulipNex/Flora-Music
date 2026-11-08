import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Replace the leftover junk
junk = r"""\.sp,\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.clickable \{\s*isAutoScrollLocked = true\s*onSeekTo\(line\.timestampMs\)\s*\}\s*\)"""
content = re.sub(junk, "", content)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
