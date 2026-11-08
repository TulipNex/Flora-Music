with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    lines = f.readlines()

start = -1
end = -1
for i, line in enumerate(lines):
    if "val contextLocal = LocalContext.current" in line and i > 1000 and i < 1120:
        start = i + 1
        break

for i in range(start, len(lines)):
    if "var parsedLyrics by remember { mutableStateOf<LyricState>(LyricState.None) }" in lines[i]:
        end = i
        break

if start != -1 and end != -1:
    new_lines = lines[:start] + lines[end:]
    with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
        f.writelines(new_lines)
    print("Replaced!")
else:
    print(f"Not found: start={start} end={end}")
