import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

# Replace Slider with MinimalSlider and map colors correctly
pattern = r'Slider\(\s*value\s*=\s*(.*?),\s*onValueChange\s*=\s*\{(.*?)\},\s*valueRange\s*=\s*(.*?),\s*colors\s*=\s*SliderDefaults\.colors\(thumbColor\s*=\s*(.*?),\s*activeTrackColor\s*=\s*(.*?),\s*inactiveTrackColor\s*=\s*(.*?)\),\s*modifier\s*=\s*(.*?)\s*\)'

replacement = r'''MinimalSlider(
                value = \1,
                onValueChange = {\2},
                valueRange = \3,
                activeColor = \5,
                inactiveColor = \6,
                modifier = \7
            )'''

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
