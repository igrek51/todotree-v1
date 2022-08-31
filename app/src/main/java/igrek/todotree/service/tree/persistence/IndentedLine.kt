package igrek.todotree.service.tree.persistence

internal class IndentedLine {
    var indentedLine: String
        private set
    var indentation: Int
        private set

    constructor(unindentedLine: String) {
        indentation = countIndentation(unindentedLine)
        indentedLine = unindentedLine.substring(indentation).trim { it <= ' ' }
    }

    constructor(indentedLine: String, indentation: Int) {
        this.indentedLine = indentedLine
        this.indentation = indentation
    }

    private fun countIndentation(unindentedLine: String): Int {
        var indentation = 0
        while (indentation < unindentedLine.length && unindentedLine[indentation] == '\t') {
            indentation++
        }
        return indentation
    }
}