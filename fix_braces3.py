import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                                    }
                                }
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            // Jarak Antar Baris (Item Spacing)"""

replacement = """                                    }
                                }
                            }
                        }
                    }
                }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            // Jarak Antar Baris (Item Spacing)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
