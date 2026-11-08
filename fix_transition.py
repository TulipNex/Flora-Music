with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_transition = """        AnimatedContent(
            targetState = showLyricsView,
            transitionSpec = {
                val duration = 400"""
new_transition = """        AnimatedContent(
            targetState = showLyricsView,
            transitionSpec = {
                val duration = 250"""

content = content.replace(old_transition, new_transition)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
