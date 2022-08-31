package igrek.todotree.service.filesystem


private fun cutSlashFromEnd(pathstr: String): String {
    var pathstr = pathstr
    while (pathstr.isNotEmpty() && pathstr[pathstr.length - 1] == '/') {
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
    )
}
