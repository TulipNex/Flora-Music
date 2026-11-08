import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

# Add showMenu state
state_target = """    var showResetDialog by remember { mutableStateOf(false) }"""
state_replacement = """    var showMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }"""
content = content.replace(state_target, state_replacement)

# Replace top bar icons and remove "+ Tab Custom" button
target_row = """        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan Tab", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            
            IconButton(onClick = { showResetDialog = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = EmeraldGreen)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tahan & geser untuk memindah",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { showAddCustomDialog = true }) {
                Text("+ Tab Custom", color = EmeraldGreen, fontSize = 14.sp)
            }
        }"""

replacement_row = """        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan Tab", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CardSlate)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tambah Tab Custom", color = Color.White) },
                        onClick = {
                            showMenu = false
                            showAddCustomDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Pengaturan", color = Color.White) },
                        onClick = {
                            showMenu = false
                            showResetDialog = true
                        }
                    )
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tahan & geser untuk memindah",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }"""

content = content.replace(target_row, replacement_row)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
