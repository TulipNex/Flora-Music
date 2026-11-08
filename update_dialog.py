import re

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Extract from fun SongMetadataDetailsDialog to the end of the dialog
# It's at the end of the file basically, but wait, there's QueueDialog etc?
# Let's check where it ends.
