import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# I will replace `kotlin.math.exp` with nothing, we use `Math.exp`
content = content.replace('import kotlin.math.exp\n', '')
content = content.replace('import kotlin.math.cos\n', '')
content = content.replace('import kotlin.math.sin\n', '')
content = content.replace('import kotlin.math.PI\n', '')
content = content.replace('import kotlin.math.abs\n', '')

content = content.replace('PI.toFloat()', 'Math.PI.toFloat()')
content = content.replace('exp(', 'Math.exp(')
content = content.replace('cos(', 'Math.cos(')
content = content.replace('sin(', 'Math.sin(')
content = content.replace('abs(', 'Math.abs(')

# Since Math.exp returns Double, we need to cast it.
# Actually, wait. Math.exp(Double) returns Double. If we multiply Float by Double, the result is Double.
# So `crescendoDeltaX = Math.exp(...).toFloat()` is needed.
# Better to use kotlin.math.* BUT specify them explicitly like `kotlin.math.exp(...)` and `kotlin.math.cos(...)`.

content = content.replace('Math.exp(', 'kotlin.math.exp(')
content = content.replace('Math.cos(', 'kotlin.math.cos(')
content = content.replace('Math.sin(', 'kotlin.math.sin(')
content = content.replace('Math.PI', 'kotlin.math.PI')
content = content.replace('Math.abs(', 'kotlin.math.abs(')

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
