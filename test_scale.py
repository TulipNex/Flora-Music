def calculate_scales(lyricsSameFontSize, lyricsActiveFontSize, lyricsOtherFontSize, distance):
    if lyricsSameFontSize:
        baseFontSize = lyricsActiveFontSize * 1.05
        activeScale = 1.0
        inactiveScale = lyricsActiveFontSize / baseFontSize
    else:
        baseFontSize = lyricsActiveFontSize
        activeScale = 1.0
        inactiveScale = lyricsOtherFontSize / baseFontSize

    if distance == 0:
        return baseFontSize, activeScale
    else:
        return baseFontSize, inactiveScale

print(calculate_scales(False, 24, 16, 0))
print(calculate_scales(False, 24, 16, 1))
print(calculate_scales(True, 24, 16, 0))
print(calculate_scales(True, 24, 16, 1))
