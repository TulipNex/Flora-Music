import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

target = """                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(if (isDragging) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),"""

replacement = """                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .then(
                                    if (isDragging) {
                                        Modifier
                                            .shadow(8.dp, RoundedCornerShape(8.dp))
                                            .background(CardSlate, RoundedCornerShape(8.dp))
                                            .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    } else {
                                        Modifier.background(Color.Transparent, RoundedCornerShape(8.dp))
                                    }
                                )
                                .padding(horizontal = 8.dp),"""

content = content.replace(target, replacement)

target_import = "import androidx.compose.ui.zIndex"
replacement_import = """import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border"""

if "import androidx.compose.ui.draw.shadow" not in content:
    content = content.replace(target_import, replacement_import)

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(content)
