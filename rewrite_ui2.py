import re

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'r') as f:
    content = f.read()

# Replace the conditional if statements
content = content.replace('if (selectedCategory == "Lagu") {', 'if (currentItems.contains("Lagu")) {')
content = content.replace('if (selectedCategory == "Album") {', 'if (currentItems.contains("Album")) {')
content = content.replace('if (selectedCategory == "Artis") {', 'if (currentItems.contains("Artis")) {')
content = content.replace('if (selectedCategory == "Favorit") {', 'if (currentItems.contains("Favorit") && currentItems.size == 1) {')
content = content.replace('if (selectedCategory == "Riwayat") {', 'if (currentItems.contains("Riwayat") && currentItems.size == 1) {')
content = content.replace('if (selectedCategory == "Teratas") {', 'if (currentItems.contains("Teratas") && currentItems.size == 1) {')
content = content.replace('if (selectedCategory == "Playlist") {', 'if (currentItems.contains("Playlist") && currentItems.size == 1) {')
content = content.replace('if (selectedCategory == "Genre") {', 'if (currentItems.contains("Genre") && currentItems.size == 1) {')

content = content.replace('if (selectedCategory == "Koleksi") {', 'if (currentItems.size > 1) {')
content = content.replace('subTabs.contains', 'currentItems.contains')

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
content = content.replace('// CONDITIONAL RENDERING BASED ON SELECTED CATEGORY', prefix)

with open('app/src/main/java/com/example/presentation/FloraUI.kt', 'w') as f:
    f.write(content)
print("Rewritten successfully")

