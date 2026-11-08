import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

target = """        // Left Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterStart)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .then(dragModifier)
        )

        // Right Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterEnd)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .then(dragModifier)
        )

        // Center Content of the lyrics
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp),"""

replacement = """        // Left Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterStart)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .zIndex(1f) // Ensure it sits on top of the lyrics to intercept drag
                .then(dragModifier)
        )

        // Right Edge Area for dragging to dismiss
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterEnd)
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .zIndex(1f) // Ensure it sits on top of the lyrics to intercept drag
                .then(dragModifier)
        )

        // Center Content of the lyrics
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),"""

if target in content:
    content = content.replace(target, replacement)
    
    # We might need to import zIndex
    if "import androidx.compose.ui.zIndex" not in content:
        content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.zIndex")
        
    with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")

