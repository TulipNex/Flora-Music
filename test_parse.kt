import java.util.regex.Pattern

fun main() {
    val TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
    val text = "[00:10.00]Hello world\n<Hello:10.0:10.5|world:10.6:11.0>\n[00:12.00]Test"
    val lines = text.split("\n", "\r\n")
    for (line in lines) {
        val trimmed = line.trim()
        val matcher = TIMESTAMP_PATTERN.matcher(trimmed)
        var hasMatch = false
        while (matcher.find()) {
            hasMatch = true
        }
        if (hasMatch) {
            println("Matched: $trimmed")
        } else {
            println("Ignored: $trimmed")
        }
    }
}
