import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

replacement = """                                        onDragEnd = {
                                            val currentDraggedIndex = allItems.indexOfFirst { it.id == item.id }
                                            val currentTargetIndex = if (currentDraggedIndex != -1) {
                                                (currentDraggedIndex + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, allItems.lastIndex)
                                            } else -1

                                            if (currentDraggedIndex != -1 && currentTargetIndex != -1 && currentDraggedIndex != currentTargetIndex) {
                                                val movedItem = allItems.removeAt(currentDraggedIndex)
                                                allItems.add(currentTargetIndex, movedItem)
                                            }
                                            draggedItemId = null
                                            dragOffset = 0f
                                            validateAndSaveFn()
                                        },"""

# Look for exactly this text
target = """                                        onDragEnd = {
                                            if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
                                                val movedItem = allItems.removeAt(draggedIndex)
                                                allItems.add(targetIndex, movedItem)
                                            }
                                            draggedItemId = null
                                            dragOffset = 0f
                                            validateAndSaveFn()
                                        },"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
