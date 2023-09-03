package igrek.todotree.util

import java.lang.StringBuilder
import java.util.*

class StringSimplifier {

    companion object {

        private val locale = Locale("pl", "PL")
        private val specialCharsMapping = mutableMapOf<Char, Char>()

        init {
            // special polish letters transformation
            specialCharsMapping['ą'] = 'a'
            specialCharsMapping['ż'] = 'z'
            specialCharsMapping['ś'] = 's'
            specialCharsMapping['ź'] = 'z'
            specialCharsMapping['ę'] = 'e'
            specialCharsMapping['ć'] = 'c'
            specialCharsMapping['ń'] = 'n'
            specialCharsMapping['ó'] = 'o'
            specialCharsMapping['ł'] = 'l'
        }

        fun simplify(s: String): String {
            var s2 = s.lowercase(locale)
            // special chars mapping
            specialCharsMapping.forEach { (k, v) -> s2 = s2.replace(k, v) }
            return s2
        }

    }

}

class EmotionLessInator {
    // regex doesn't work with unicode code points for some reason
    private val emojiFilterRegex = "\\u00a9|\\u00ae|[\\u2000-\\u3300]|\\ud83c[\\ud000-\\udfff]|\\ud83d[\\ud000-\\udfff]|\\ud83e[\\ud000-\\udfff]".toRegex()
    private val locale = Locale("pl", "PL")

    fun simplify(text: String): String {
        return emojiLess(text)
            .lowercase(locale)
            .trim()
    }

    private fun emojiLess(text: String): String {
        val buffer = StringBuilder()
        var i = 0
        while (i < text.length) {
            val char: Char = text[i]
            if (isCharEmoji(char)) {
                i++
            } else if (i + 1 < text.length && is2CharEmoji(char, text[i + 1])) {
                i += 2
            } else {
                i++
                buffer.append(char)
            }
        }
        return buffer.toString()
    }

    private fun isCharEmoji(char: Char): Boolean {
        return when (char) {
            '\u00a9', '\u00ae' -> true
            in '\u2000'..'\u3300' -> true
            else -> false
        }
    }

    private fun is2CharEmoji(char: Char, next: Char): Boolean {
        return when {
            char == '\ud83c' && next in '\ud000'..'\udfff' -> true
            char == '\ud83d' && next in '\ud000'..'\udfff' -> true
            char == '\ud83e' && next in '\ud000'..'\udfff' -> true
            else -> false
        }
    }
}
