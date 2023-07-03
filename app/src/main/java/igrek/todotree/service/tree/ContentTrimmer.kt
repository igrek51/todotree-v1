package igrek.todotree.service.tree

class ContentTrimmer {

    fun trimContent(_content: String): String {
        // remove unallowed characteres
        val content = _content.replace("\t", "")
        return content.trim()
    }
}