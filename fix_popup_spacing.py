import re

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'r') as f:
    content = f.read()

target1 = """                        DropdownMenu(
                            expanded = showAlignmentDialog,
                            onDismissRequest = { showAlignmentDialog = false },
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {"""

replacement1 = """                        DropdownMenu(
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

content = content.replace(target1, replacement1)

target2 = """                        DropdownMenu(
                            expanded = showPositionDialog,
                            onDismissRequest = { showPositionDialog = false },
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {"""

replacement2 = """                        DropdownMenu(
                            expanded = showPositionDialog,
                            onDismissRequest = { showPositionDialog = false },
                            modifier = Modifier
                                .width(120.dp)
                                .background(CardSlate, RoundedCornerShape(8.dp))
                                .padding(3.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/FloraLyricsSettingsUI.kt', 'w') as f:
    f.write(content)
