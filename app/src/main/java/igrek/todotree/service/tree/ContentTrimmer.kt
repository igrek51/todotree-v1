package igrek.todotree.service.tree

class ContentTrimmer {

    companion object {
        private const val WHITE_CHARS = " "
        private const val INVALID_CHARS = "{}[]\n\t"
    }

    fun trimContent(_content: String): String {
        // remove unallowed characteres
        var content = _content
        var i = 0
        while (i < content.length) {
            if (isCharInSet(content[i], INVALID_CHARS)) {
                content = content.substring(0, i) + content.substring(i + 1)
                i--
            }
            i++
        }
        // trim whitespaces from beginning
        while (content.isNotEmpty() && isCharInSet(content[0], WHITE_CHARS)) {
            content = content.substring(1)
        }
        // trim whitespaces from the end
        while (content.isNotEmpty() && isCharInSet(content[content.length - 1], WHITE_CHARS)) {
            content = content.substring(0, content.length - 1)
        }
        return content
    }

    private fun isCharInSet(c: Char, set: String): Boolean {
        for (element in set) {
            if (element == c) return true
        }
        return false
    }
}