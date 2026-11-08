import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

# We need to find the `// CONDITIONAL RENDERING BASED ON SELECTED CATEGORY`
# and the `// Bottom spacing`
start_idx = content.find('// CONDITIONAL RENDERING BASED ON SELECTED CATEGORY')
end_idx = content.find('// Bottom spacing', start_idx)

if start_idx != -1 and end_idx != -1:
    block = content[start_idx:end_idx]
    
    # We will replace `if (selectedCategory == "Lagu") {` with `if (currentItems.contains("Lagu")) {`
    new_block = block.replace('if (selectedCategory == "Lagu") {', 'if (currentItems.contains("Lagu")) {')
    new_block = new_block.replace('if (selectedCategory == "Album") {', 'if (currentItems.contains("Album")) {')
    new_block = new_block.replace('if (selectedCategory == "Artis") {', 'if (currentItems.contains("Artis")) {')
    new_block = new_block.replace('if (selectedCategory == "Favorit") {', 'if (currentItems.contains("Favorit") && currentItems.size == 1) {')
    new_block = new_block.replace('if (selectedCategory == "Riwayat") {', 'if (currentItems.contains("Riwayat") && currentItems.size == 1) {')
    new_block = new_block.replace('if (selectedCategory == "Teratas") {', 'if (currentItems.contains("Teratas") && currentItems.size == 1) {')
    new_block = new_block.replace('if (selectedCategory == "Playlist") {', 'if (currentItems.contains("Playlist") && currentItems.size == 1) {')
    new_block = new_block.replace('if (selectedCategory == "Genre") {', 'if (currentItems.contains("Genre") && currentItems.size == 1) {')
    
    # For Koleksi block, it renders horizontally for Favorit, Riwayat, Teratas, Playlist, Genre.
    # We want ANY tab with multiple items to do this, so we replace `if (selectedCategory == "Koleksi") {`
    # with `if (currentItems.size > 1) {`
    # AND inside it, we replace `subTabs.contains("...")` with `currentItems.contains("...")`
    new_block = new_block.replace('if (selectedCategory == "Koleksi") {', 'if (currentItems.size > 1) {')
    new_block = new_block.replace('subTabs.contains', 'currentItems.contains')
    
    # Prepend the currentItems variable logic
    prefix = """// CONDITIONAL RENDERING BASED ON SELECTED CATEGORY
            val currentConfig = tabConfigs.find { it.name == selectedCategory }
            val currentItems = currentConfig?.items ?: emptyList()
            if (currentItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("Tab kosong.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
"""
    # Remove the original comment to avoid duplication
    new_block = new_block.replace('// CONDITIONAL RENDERING BASED ON SELECTED CATEGORY', '')
    
    final_block = prefix + new_block
    
    content = content[:start_idx] + final_block + content[end_idx:]
    
    with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
        f.write(content)
    print("Rewritten successfully")
else:
    print("Markers not found")

