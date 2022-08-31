package igrek.todotree.service.filesystem

import igrek.todotree.service.filesystem.PathBuilder
import java.io.File

class PathBuilder {
    private var pathstr: String

    internal constructor(pathstr: String) {
        this.pathstr = cutSlashFromEnd(pathstr)
    }

    /**
     * copy constructor
     * @param src source
     */
    private constructor(src: PathBuilder) {
        pathstr = src.pathstr
    }

    override fun toString(): String {
        return pathstr
    }

    val file: File
        get() = File(pathstr)

    fun append(pathstr: String): PathBuilder {
        val newPathstr = cutSlashFromEnd(this.pathstr) + "/" + trimSlash(pathstr)
        return PathBuilder(newPathstr)
    }

    fun parent(): PathBuilder? {
        if (pathstr == "/") return null
        val copy = PathBuilder(this)
        val lastSlash = cutSlashFromEnd(copy.pathstr).lastIndexOf("/")
        if (lastSlash < 0) return null
        copy.pathstr = copy.pathstr.substring(0, lastSlash)
        return copy
    }

    companion object {
        private fun cutSlashFromEnd(pathstr: String): String {
            var pathstr = pathstr
            while (pathstr.length > 0 && pathstr[pathstr.length - 1] == '/') {
                pathstr = pathstr.substring(0, pathstr.length - 1)
            }
            return pathstr
        }

        private fun trimSlash(pathstr: String): String {
            var pathstr = pathstr
            while (pathstr.length > 0 && pathstr[0] == '/') {
                pathstr = pathstr.substring(1)
            }
            while (pathstr.length > 0 && pathstr[pathstr.length - 1] == '/') {
                pathstr = pathstr.substring(0, pathstr.length - 1)
            }
            return pathstr
        }

        fun removeExtension(pathStr: String): String {
            val lastDot = cutSlashFromEnd(pathStr).lastIndexOf(".")
            val lastSlash = cutSlashFromEnd(pathStr).lastIndexOf("/")
            if (lastDot < 0) return pathStr //nie ma kropki
            return if (lastDot < lastSlash) pathStr else pathStr.substring(
                0,
                lastDot
            ) //kropka jest przed ostatnim slashem
        }
    }
}