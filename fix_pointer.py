import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

replacement = """                            .pointerInput(item.id) {
                                if (item.role != TabRole.HEADER) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { 
                                            draggedItemId = item.id
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggedItemId == null) return@detectDragGesturesAfterLongPress
                                            dragOffset += dragAmount.y
                                        },
                                        onDragEnd = {
                                            if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
                                                val movedItem = allItems.removeAt(draggedIndex)
                                                allItems.add(targetIndex, movedItem)
                                            }
                                            draggedItemId = null
                                            dragOffset = 0f
                                            validateAndSaveFn()
                                        },
                                        onDragCancel = {
                                            draggedItemId = null
                                            dragOffset = 0f
                                        }
                                    )
                                }
                            },"""

content = re.sub(
    r'                            \.pointerInput\(item\.id\) \{.*?                                \)\n                            \},',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
