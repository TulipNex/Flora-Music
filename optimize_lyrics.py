with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

old_block = """                            lines.forEachIndexed { listIndex, line ->
                                val distance = kotlin.math.abs(listIndex - activeIndexClean)
                                val targetOffset = anchorY + (positions[listIndex] ?: ((listIndex - activeIndexClean).toFloat() * fallbackLineHeight))
                                val frozenOffset = remember { mutableFloatStateOf(targetOffset) }"""

new_block = """                            lines.forEachIndexed { listIndex, line ->
                                val distance = kotlin.math.abs(listIndex - activeIndexClean)
                                val targetOffset = anchorY + (positions[listIndex] ?: ((listIndex - activeIndexClean).toFloat() * fallbackLineHeight))
                                
                                // CULL OUT-OF-BOUNDS ITEMS to prevent massive frame drops
                                val currentEstimatedY = targetOffset + userManualOffset
                                val isVisible = currentEstimatedY > -1000f && currentEstimatedY < maxHeightPx + 1000f
                                
                                if (isVisible) {
                                val frozenOffset = remember { mutableFloatStateOf(targetOffset) }"""

content = content.replace(old_block, new_block)

old_block_end = """                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

new_block_end = """                                        }
                                    }
                                }
                                } // end isVisible
                            }
                        }
                    }
                }
            }"""
content = content.replace(old_block_end, new_block_end)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
