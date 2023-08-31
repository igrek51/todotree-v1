package igrek.todotree.ui.edititem

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

class NumericTyper(
    private val state: EditItemState,
) {

    fun toggleTypingHour() {
        state.typingDate.value = false
        when (state.typingHour.value) {
            true -> {
                closeNumericKeyboard()
                state.typingHour.value = false
            }
            false -> {
                state.typingHour.value = true
                state.numericKeyboard.value = true
                state.numericBuffer.value = ""
            }
        }
    }

    fun toggleTypingDate() {
        state.typingHour.value = false
        when (state.typingDate.value) {
            true -> {
                closeNumericKeyboard()
                state.typingDate.value = false
            }
            false -> {
                state.typingDate.value = true
                state.numericKeyboard.value = true
                state.numericBuffer.value = ""
            }
        }
    }

    fun toggleTypingNumeric() {
        if (state.numericKeyboard.value)
            finishNumericTyping()
        state.typingHour.value = false
        state.typingDate.value = false
        state.numericKeyboard.value = !state.numericKeyboard.value
        state.numericBuffer.value = ""
    }

    fun closeNumericKeyboard() {
        finishNumericTyping()
        state.numericKeyboard.value = false
        state.typingHour.value = false
        state.typingDate.value = false
    }

    fun onKeyUp(keyCode: Int, key: Char) {
        when {
            keyCode in 7 .. 16 && (state.typingHour.value || state.typingDate.value) -> { // 0-9 numbers
                state.numericBuffer.value += key
                checkNumericTypingBuffer()
            }
            keyCode == 67 -> { // backspace
                if (state.numericBuffer.value.isNotEmpty()) {
                    state.numericBuffer.value = state.numericBuffer.value.dropLast(1)
                }
            }
            else -> finishNumericTyping()
        }
    }

    private fun checkNumericTypingBuffer() {
        val buffer = state.numericBuffer.value
        when {
            state.typingHour.value && buffer.length >= 4 -> closeNumericKeyboard()
            state.typingDate.value && buffer.length >= 4 -> closeNumericKeyboard()
        }
    }

    fun finishNumericTyping() {
        val buffer = state.numericBuffer.value
        val text = state.textFieldValue.value.text
        var cursor = state.textFieldValue.value.selection.max
        when {
            state.typingHour.value && buffer.length >= 3 -> { // hour 01:02 or 1:02
                val edited = text.insertAt(":", cursor - 2)
                cursor += 1
                state.textFieldValue.value = TextFieldValue(
                    text = edited,
                    selection = TextRange(cursor, cursor),
                )
                state.typingHour.value = false
            }
            state.typingDate.value && buffer.length >= 3 -> { // date 01.02 or 1.02
                val edited = text.insertAt(".", cursor - 2)
                cursor += 1
                state.textFieldValue.value = TextFieldValue(
                    text = edited,
                    selection = TextRange(cursor, cursor),
                )
                state.typingDate.value = false
            }
        }
        state.numericBuffer.value = ""
    }

}

private fun String.insertAt(c: String, offset: Int): String {
    var mOffset = offset
    if (mOffset < 0) mOffset = 0
    if (mOffset > this.length) mOffset = this.length
    val before = this.substring(0, mOffset)
    val after = this.substring(mOffset)
    return before + c + after
}
