import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

# We need to find the `Column` with `verticalScroll`
# and replace its content.
import sys

start_idx = content.find("Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .verticalScroll(rememberScrollState())\n                .padding(bottom = 82.dp)\n        ) {")

if start_idx == -1:
    print("Could not find start of Column")
    sys.exit(1)

# Find the matching closing brace for this Column
brace_count = 0
end_idx = -1
in_block = False

for i in range(start_idx, len(content)):
    if content[i] == '{':
        if not in_block:
            in_block = True
        brace_count += 1
    elif content[i] == '}':
        brace_count -= 1
        if in_block and brace_count == 0:
            end_idx = i
            break

if end_idx == -1:
    print("Could not find end of Column")
    sys.exit(1)

new_column_content = """Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {
            allItems.forEachIndexed { index, item ->
                key(item.id) {
                    val isDragging = item.id == draggedItemId
                    val zIndex = if (isDragging) 1f else 0f
                    val translationY = if (isDragging) dragOffset else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp)
                            .zIndex(zIndex)
                            .graphicsLayer { this.translationY = translationY }
                            .pointerInput(item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { 
                                        draggedItemId = item.id
                                        dragOffset = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (draggedItemId == null) return@detectDragGesturesAfterLongPress
                                        
                                        val currentDraggedIndex = allItems.indexOfFirst { it.id == draggedItemId }
                                        if (currentDraggedIndex == -1) return@detectDragGesturesAfterLongPress
                                        
                                        dragOffset += dragAmount.y
                                        
                                        if (dragOffset > itemHeightPx * 0.5f && currentDraggedIndex < allItems.lastIndex) {
                                            val nextIdx = currentDraggedIndex + 1
                                            val temp = allItems[currentDraggedIndex]
                                            allItems[currentDraggedIndex] = allItems[nextIdx]
                                            allItems[nextIdx] = temp
                                            dragOffset -= itemHeightPx
                                        } else if (dragOffset < -itemHeightPx * 0.5f && currentDraggedIndex > 0) {
                                            val prevIdx = currentDraggedIndex - 1
                                            val temp = allItems[currentDraggedIndex]
                                            allItems[currentDraggedIndex] = allItems[prevIdx]
                                            allItems[prevIdx] = temp
                                            dragOffset += itemHeightPx
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemId = null
                                        dragOffset = 0f
                                        saveFn(allItems)
                                    },
                                    onDragCancel = {
                                        draggedItemId = null
                                        dragOffset = 0f
                                        saveFn(allItems)
                                    }
                                )
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val icon = when (item.name) {
                            "Lagu" -> Icons.Default.MusicNote
                            "Album" -> Icons.Default.Album
                            "Artis" -> Icons.Default.Person
                            "Favorit" -> Icons.Default.Favorite
                            "Riwayat" -> Icons.Default.History
                            "Teratas" -> Icons.Default.TrendingUp
                            "Playlist" -> Icons.Default.PlaylistPlay
                            "Genre" -> Icons.Default.Category
                            else -> Icons.Default.Folder
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(if (isDragging) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DragHandle, contentDescription = "Geser", tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            if (item.role == TabRole.SUB) {
                                Spacer(modifier = Modifier.width(24.dp))
                                Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.name, 
                                color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, 
                                fontSize = 16.sp, 
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (item.isCustom) {
                                IconButton(onClick = {
                                    allItems.remove(item)
                                    saveFn(allItems)
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val nextRole = when (item.role) {
                                            TabRole.MAIN -> TabRole.SUB
                                            TabRole.SUB -> TabRole.HIDDEN
                                            TabRole.HIDDEN -> TabRole.MAIN
                                        }
                                        val idx = allItems.indexOf(item)
                                        if (idx != -1) {
                                            allItems[idx] = item.copy(role = nextRole)
                                            saveFn(allItems)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(item.role.label, fontSize = 12.sp, color = if (item.role == TabRole.HIDDEN) TextSecondary else EmeraldGreen)
                                }
                            }
                        }
                    }
                }
            }
        }"""

# Before replacing, we need to ensure draggedItemId is used instead of draggedIndex
with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    full_content = f.read()
    
full_content = full_content.replace(
    "var draggedIndex by remember { mutableStateOf<Int?>(null) }",
    "var draggedItemId by remember { mutableStateOf<String?>(null) }"
)

start_idx2 = full_content.find("Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .verticalScroll(rememberScrollState())\n                .padding(bottom = 82.dp)\n        ) {")
if start_idx2 == -1:
    print("Could not find start of Column in second pass")
    sys.exit(1)

brace_count = 0
end_idx2 = -1
in_block = False
for i in range(start_idx2, len(full_content)):
    if full_content[i] == '{':
        if not in_block:
            in_block = True
        brace_count += 1
    elif full_content[i] == '}':
        brace_count -= 1
        if in_block and brace_count == 0:
            end_idx2 = i
            break

final_content = full_content[:start_idx2] + new_column_content + full_content[end_idx2+1:]

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(final_content)

print("Replaced column successfully")

