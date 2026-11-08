import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

top_row = """        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan Tab", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            
            IconButton(onClick = { viewModel.resetTabsToDefault() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = EmeraldGreen)
            }
        }"""

content = re.sub(
    r'Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\),\s*verticalAlignment = Alignment\.CenterVertically\s*\)\s*\{\s*IconButton\(onClick = onBackClick\) \{\s*Icon\(Icons\.Default\.ArrowBack, contentDescription = "Kembali", tint = Color\.White\)\s*\}\s*Spacer\(modifier = Modifier\.width\(16\.dp\)\)\s*Text\("Pengaturan Tab", fontSize = 20\.sp, fontWeight = FontWeight\.Bold, color = Color\.White\)\s*\}',
    top_row,
    content
)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
