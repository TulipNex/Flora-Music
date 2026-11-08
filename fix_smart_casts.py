with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

content = content.replace("val palette = androidx.palette.graphics.Palette.from(artBitmap).generate()", "val palette = androidx.palette.graphics.Palette.from(artBitmap!!).generate()")
content = content.replace("val palette = androidx.palette.graphics.Palette.from(artBitmap2).generate()", "val palette = androidx.palette.graphics.Palette.from(artBitmap2!!).generate()")

content = content.replace("bitmap = artBitmap.asImageBitmap()", "bitmap = artBitmap!!.asImageBitmap()")
content = content.replace("bitmap = artBitmap2.asImageBitmap()", "bitmap = artBitmap2!!.asImageBitmap()")

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
