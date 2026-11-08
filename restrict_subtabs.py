import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                            } else if (item.role != TabRole.HIDDEN) {
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
                            }"""

replacement = """                            } else if (item.role != TabRole.HIDDEN) {
                                if (item.name !in listOf("Lagu", "Artis", "Album")) {
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
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .background(CardSlate, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.role.label, fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
