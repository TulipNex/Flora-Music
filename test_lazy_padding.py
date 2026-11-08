import subprocess
print("Let's look at Compose documentation or remember.")
# Actually, when you have contentPadding(top = 100.dp) and you scroll to top, 
# visibleItemsInfo[0].offset is 0? Or 100?
# In Compose 1.4+, offset is relative to the top of the viewport minus the padding, or something?
# No, offset is relative to the start of the layout's bound.
