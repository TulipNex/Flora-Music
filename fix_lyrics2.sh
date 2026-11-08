sed -i '1711a\
            val activeLineIndices = remember(activeIndex, lines) {\
                val set = mutableSetOf<Int>()\
                if (activeIndex != -1) {\
                    set.add(activeIndex)\
                    val activeLine = lines[activeIndex]\
                    if (activeLine.isBackground) {\
                        val pairedMain = (activeIndex - 1 downTo 0).firstOrNull { !lines[it].isBackground }\
                        if (pairedMain != null) set.add(pairedMain)\
                    }\
                }\
                set\
            }' app/src/main/java/com/example/presentation/FloraPlayerUI.kt
