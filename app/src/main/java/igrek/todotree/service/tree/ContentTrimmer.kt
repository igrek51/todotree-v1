package igrek.todotree.service.tree

class ContentTrimmer {
    val WHITE_CHARS = " "
    val INVALID_CHARS = "{}[]\n\t"
    fun trimContent(content: String): String {
        // remove unallowed characteres
        var content = content
        var i = 0
        while (i < content.length) {
            if (isCharInSet(content[i], INVALID_CHARS)) {
                content = content.substring(0, i) + content.substring(i + 1)
                i--
            }
            i++
        }
        // trim whitespaces from beginning
        while (content.length > 0 && isCharInSet(content[0], WHITE_CHARS)) {
            content = content.substring(1)
        }
        // trim whitespaces from the end
        while (content.length > 0 && isCharInSet(content[content.length - 1], WHITE_CHARS)) {
            content = content.substring(0, content.length - 1)
        }
        return content
    }

    private fun isCharInSet(c: Char, set: String): Boolean {
        for (i in 0 until set.length) {
            if (set[i] == c) return true
        }
        return false
    }
}