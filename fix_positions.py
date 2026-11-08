with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

print("Occurrences of remember(itemHeights.toMap():", content.count("remember(itemHeights.toMap()"))
