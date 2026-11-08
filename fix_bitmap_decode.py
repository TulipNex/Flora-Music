import re
with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'r') as f:
    content = f.read()

# Pattern for artBitmap decoding
old_decode = """    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                null
            }
        }
    }"""
new_decode = """    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            artBitmap = null
        }
    }"""
content = content.replace(old_decode, new_decode)

old_decode_2 = """    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
        }
    }"""
new_decode_2 = """    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try { android.graphics.BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size) } catch (e: Exception) { null }
            }
        } else {
            artBitmap = null
        }
    }"""
content = content.replace(old_decode_2, new_decode_2)

old_decode_3 = """    val artBitmap = remember(songArtBytes) {
        songArtBytes?.let {
            try {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (e: Exception) {
                null
            }
        }
    }"""
new_decode_3 = """    var artBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes) {
        if (songArtBytes != null) {
            artBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    android.graphics.BitmapFactory.decodeByteArray(songArtBytes!!, 0, songArtBytes!!.size)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            artBitmap = null
        }
    }"""
content = content.replace(old_decode_3, new_decode_3)

old_decode_4 = """    val artBitmap2 = remember(songArtBytes2) {
        songArtBytes2?.let {
            try { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) } catch (e: Exception) { null }
        }
    }"""
new_decode_4 = """    var artBitmap2 by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(songArtBytes2) {
        if (songArtBytes2 != null) {
            artBitmap2 = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try { android.graphics.BitmapFactory.decodeByteArray(songArtBytes2!!, 0, songArtBytes2!!.size) } catch (e: Exception) { null }
            }
        } else {
            artBitmap2 = null
        }
    }"""
content = content.replace(old_decode_4, new_decode_4)

with open('app/src/main/java/com/example/presentation/FloraPlayerUI.kt', 'w') as f:
    f.write(content)
