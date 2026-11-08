import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

# Add dialog state
dialog_state = """    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }"""

content = content.replace("var showAddCustomDialog by remember { mutableStateOf(false) }", dialog_state)

# Add dialog UI
reset_dialog = """
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Pengaturan Tab") },
            text = { Text("Apakah Anda yakin ingin mengembalikan pengaturan tab ke bawaan? Semua tab custom akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTabsToDefault()
                        showResetDialog = false
                    }
                ) { Text("Ya, Reset", color = EmeraldGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
            }
        )
    }
"""

content = content.replace("if (showAddCustomDialog) {", reset_dialog + "    if (showAddCustomDialog) {")

# Update IconButton onClick
content = content.replace(
    "IconButton(onClick = { viewModel.resetTabsToDefault() }) {",
    "IconButton(onClick = { showResetDialog = true }) {"
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
