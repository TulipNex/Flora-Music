import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """                        DropdownMenu(
                            expanded = showAlignmentDialog,
                            onDismissRequest = { showAlignmentDialog = false },
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(3.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {"""

replacement1 = """                        if (showAlignmentDialog) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopStart,
                                offset = androidx.compose.ui.unit.IntOffset(0, 110),
                                onDismissRequest = { showAlignmentDialog = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .background(CardSlate, RoundedCornerShape(8.dp))
                                        .padding(3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {"""

content = content.replace(target1, replacement1)
content = content.replace(
"""                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Posisi Lirik Aktif""",
"""                                }
                            }
                        }
                    }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Posisi Lirik Aktif"""
)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
