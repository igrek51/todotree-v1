package igrek.todotree.service.filesystem


private fun cutSlashFromEnd(pathstr: String): String {
    var mPathStr = pathstr
    while (mPathStr.isNotEmpty() && mPathStr[mPathStr.length - 1] == '/') {
        mPathStr = mPathStr.substring(0, mPathStr.length - 1)
    }
    return mPathStr
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
