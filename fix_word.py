with open('FloraWordLevelLyrics.kt', 'r') as f:
    c = f.read()

c = c.replace('wordItem.startTime * 1000', 'wordItem.startTimeMs')
c = c.replace('wordItem.endTime * 1000', 'wordItem.endTimeMs')
c = c.replace('wordItem.startTimeMs * 1000', 'wordItem.startTimeMs')
c = c.replace('wordItem.endTimeMs * 1000', 'wordItem.endTimeMs')

c = c.replace('effectiveWords[currentGroup.first()].startTime * 1000', 'effectiveWords[currentGroup.first()].startTimeMs')
c = c.replace('word.endTime * 1000', 'word.endTimeMs')
c = c.replace('word.startTime * 1000', 'word.startTimeMs')

with open('FloraWordLevelLyrics.kt', 'w') as f:
    f.write(c)
