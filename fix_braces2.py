import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """                                    }
                                }
                            }
                        }
                    }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Posisi Lirik Aktif"""

replacement1 = """                                    }
                                }
                            }
                        }
                    }
                }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Posisi Lirik Aktif"""

content = content.replace(target1, replacement1)

target2 = """                                    }
                                }
                            }
                        }
                    }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Preview"""

replacement2 = """                                    }
                                }
                            }
                        }
                    }
                }
                }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Preview"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
