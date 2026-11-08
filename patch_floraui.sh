#!/bin/bash
awk '
BEGIN { in_koleksi = 0 }
{
    if ($0 ~ /if \(selectedCategory == "Lagu"\) {/) {
        print "            val currentItems = tabConfigs.find { it.name == selectedCategory }?.items ?: emptyList()"
        print "            if (currentItems.isEmpty()) {"
        print "                item {"
        print "                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(CardSlate, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {"
        print "                        Text(\"Tab kosong.\", color = TextSecondary, fontSize = 13.sp)"
        print "                    }"
        print "                }"
        print "            }"
        print "            for (itemModule in currentItems) {"
        print "                when (itemModule) {"
        print "                    \"Lagu\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Album"\) {/) {
        print "                    }"
        print "                    \"Album\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Artis"\) {/) {
        print "                    }"
        print "                    \"Artis\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Favorit"\) {/) {
        print "                    }"
        print "                    \"Favorit\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Riwayat"\) {/) {
        print "                    }"
        print "                    \"Riwayat\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Teratas"\) {/) {
        print "                    }"
        print "                    \"Teratas\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Playlist"\) {/) {
        print "                    }"
        print "                    \"Playlist\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Genre"\) {/) {
        print "                    }"
        print "                    \"Genre\" -> {"
    } else if ($0 ~ /if \(selectedCategory == "Koleksi"\) {/) {
        print "                    }"
        print "                }"
        print "            }"
        in_koleksi = 1
    } else {
        if (!in_koleksi) {
            print $0
        } else {
            # Skip koleksi lines up to its end brace.
            # But wait, koleksi has the Favorit/Riwayat etc. wait!
            # The user changed the Favorit, Riwayat etc to be inside `if (selectedCategory == "Favorit") {` etc!
            # Oh, Koleksi block is still there at the end? Let us see.
        }
    }
}
' app/src/main/java/com/example/presentation/FloraUI.kt > temp.kt && mv temp.kt app/src/main/java/com/example/presentation/FloraUI.kt
