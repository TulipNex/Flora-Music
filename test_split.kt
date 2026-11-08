fun main() {
    val text = "Line 1\r\nLine 2\nLine 3"
    val lines = text.split("\n", "\r\n")
    for (line in lines) {
        println("Line: [${line}]")
    }
}
