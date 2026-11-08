import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                            if (item.isCustom) {
                                IconButton(onClick = {
                                    allItems.remove(item)
                                    validateAndSaveFn()
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            } else if (item.role != TabRole.HIDDEN) {"""

replacement = """                            if (item.isCustom) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            val nextRole = when (item.role) {
                                                TabRole.MAIN -> TabRole.SUB
                                                else -> TabRole.MAIN
                                            }
                                            val idx = allItems.indexOf(item)
                                            if (idx != -1) {
                                                allItems[idx] = item.copy(role = nextRole)
                                                validateAndSaveFn()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(item.role.label, fontSize = 12.sp, color = EmeraldGreen)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = {
                                        allItems.remove(item)
                                        validateAndSaveFn()
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            } else if (item.role != TabRole.HIDDEN) {"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
