import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target1 = """                                    listState.animateScrollToItem(
                                        index = activeIndexClean,
                                        scrollOffset = targetOffset.toInt()
                                    )"""

replacement1 = """                                    listState.animateScrollToItem(
                                        index = activeIndexClean
                                    )"""

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Fixed auto-scroll 1")

target2 = """                        listState.animateScrollToItem(
                            index = activeIndexClean,
                            scrollOffset = targetOffset.toInt()
                        )"""

replacement2 = """                        listState.animateScrollToItem(
                            index = activeIndexClean
                        )"""
                        
if target2 in content:
    content = content.replace(target2, replacement2)
    print("Fixed auto-scroll 2")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)

