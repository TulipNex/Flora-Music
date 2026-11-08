import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_bracket = """                if (isBackground) {
                    var t = it
                    if (wordIdx == 0) t = t.removePrefix("(")
                    if (wordIdx == effectiveWords.size - 1) t = t.removeSuffix(")")
                    t
                } else it"""
new_bracket = """                if (isBackground) {
                    var t = it
                    if (wordIdx == 0) t = t.replace(Regex("^\\\\s*\\\\("), "")
                    if (wordIdx == effectiveWords.size - 1) t = t.replace(Regex("\\\\)\\\\s*$"), "")
                    t
                } else it"""
content = content.replace(old_bracket, new_bracket)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
