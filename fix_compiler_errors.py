import re

# Fix FloraLyricsSettingsUI.kt (MusicScanner import)
with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'com.example.domain.model.MusicScanner.getArtworkBytes(it)',
    'com.example.data.scanner.MusicScanner.getArtworkBytes(it)'
)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)

# Fix FloraPlayerUI.kt (artDominantColor undefined error)
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()
    
# The issue is that artDominantColor was defined in FloraPlayerUI but there are multiple usages 
# in separate composables (like the lyrics view at the bottom). We need to lift the variable
# or properly manage it where the lyrics view uses it. 
# Looking at the code, activeColorStr and otherColorStr were originally defined in two places
# (around line 1121 and 1431). The artDominantColor was only defined once around line 100.
# The lyrics view seems to be nested or separated. Let's fix this by re-extracting it
# properly or using the ViewModel.
# Since we already modified FloraPlayerUI.kt, let's revert it and do it properly by adding 
# the dominant color to the ViewModel instead, or just lifting it.

