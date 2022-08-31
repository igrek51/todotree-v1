package igrek.todotree.service.tree.persistence

internal class IndentedLine(unindentedLine: String) {

    var indentedLine: String
        private set
    var indentation: Int
        private set

    init {
        indentation = countIndentation(unindentedLine)
        indentedLine = unindentedLine.substring(indentation).trim { it <= ' ' }
    }

    private fun countIndentation(unindentedLine: String): Int {
        var indentation = 0
        while (indentation < unindentedLine.length && unindentedLine[indentation] == '\t') {
            indentation++
        }
        return indentation
    }
}