package igrek.todotree.ui.numkeyboard

import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.util.AttributeSet
import android.widget.EditText
import igrek.todotree.R
import igrek.todotree.intent.GUICommand

class NumericKeyboardView : KeyboardView, OnKeyboardActionListener {

    private var _context: Context
    private var editText: EditText? = null
    private var listener: NumKeyboardListener? = null
    private var input = StringBuffer()

    var typingMode = 0 //1 - hour, 2 - date, 3 - number
        private set

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this._context = context
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this._context = context
    }

    fun init(listener: NumKeyboardListener?, editText: EditText?) {
        this.editText = editText
        this.listener = listener
        val keyboard = Keyboard(context, R.xml.numeric_keyboard)
        setKeyboard(keyboard)
        visibility = GONE
        isEnabled = false
        isPreviewEnabled = true
        onKeyboardActionListener = this
    }

    var isVisible: Boolean
        get() {
            when (visibility) {
                VISIBLE -> return true
                GONE, INVISIBLE -> return false
            }
            return false
        }
        set(visible) {
            if (visible == isVisible) {
                return
            }
            if (visible) {
                visibility = VISIBLE
                isEnabled = true
            } else {
                visibility = GONE
                isEnabled = false
            }
        }

    override fun swipeDown() {
        hideAndBack()
    }

    override fun swipeRight() {
        super.swipeRight()
    }

    override fun swipeLeft() {
        super.swipeLeft()
    }

    override fun swipeUp() {
        super.swipeUp()
    }

    override fun onText(text: CharSequence) {}

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        when (primaryCode) {
            in 48..57 -> {
                typedNumber(primaryCode.toChar())
            }
            45 -> {
                typedMinus()
            }
            44 -> {
                typedComma()
            }
            -6 -> {
                typedOK()
            }
            -3 -> {
                typedBackspace()
            }
            -4 -> { // _-_
                typedHyphen()
            }
            -5 -> { //spacja
                typedSpace()
            }
        }
    }

    override fun onPress(primaryCode: Int) {}

    override fun onRelease(primaryCode: Int) {}

    private fun typedNumber(c: Char) {
        input.append(c)
        insertString("" + c)
        if (typingMode == 1) { //godzina
            if (input.length >= 4) {
                finishTyping()
                hideAndBack()
            }
        } else if (typingMode == 2) { //data
            if (input.length >= 6) {
                finishTyping()
                hideAndBack()
            }
        }
    }

    private fun typedMinus() {
        finishTyping()
        insertString("-")
    }

    private fun typedComma() {
        finishTyping()
        insertString(",")
    }

    private fun typedSpace() {
        finishTyping()
        insertString(" ")
    }

    private fun typedOK() {
        finishTyping()
        hideAndBack()
    }

    private fun typedHyphen() {
        GUICommand().numKeyboardHyphenTyped()
    }

    private fun typedBackspace() {
        if (input.isNotEmpty()) {
            input.delete(input.length - 1, input.length)
        }
        val selStart = editText!!.selectionStart
        val selEnd = editText!!.selectionEnd
        var edited = editText!!.text.toString()
        val before = edited.substring(0, selStart)
        val after = edited.substring(selEnd)
        if (selStart == selEnd) {
            if (selStart > 0) {
                edited = before.substring(0, before.length - 1) + after
                editText!!.setText(edited)
                editText!!.setSelection(selStart - 1, selEnd - 1)
            }
        } else {
            edited = before + after
            editText!!.setText(edited)
            editText!!.setSelection(selStart, selStart)
        }
    }

    private fun insertAt(str: String, c: String, _offset: Int): String {
        var offset = _offset
        if (offset < 0) offset = 0
        if (offset > str.length) offset = str.length
        val before = str.substring(0, offset)
        val after = str.substring(offset)
        return before + c + after
    }

    private fun insertString(str: String) {
        val selStart = editText!!.selectionStart
        val selEnd = editText!!.selectionEnd
        var edited = editText!!.text.toString()
        val before = edited.substring(0, selStart)
        val after = edited.substring(selEnd)
        edited = before + str + after
        editText!!.setText(edited)
        editText!!.setSelection(selStart + str.length, selStart + str.length)
    }

    fun startTyping(mode: Int) {
        typingMode = mode
        input = StringBuffer()
    }

    fun finishTyping() {
        var cursorStart = editText!!.selectionStart
        val cursorEnd = editText!!.selectionEnd
        var edited = editText!!.text.toString()
        if (cursorStart != cursorEnd) {
            input = StringBuffer()
            return
        }
        if (typingMode == 1) { //hour
            if (input.length >= 3) { // 01:02, 1:02
                edited = insertAt(edited, ":", cursorStart - 2)
                cursorStart++
                editText!!.setText(edited)
            }
        } else if (typingMode == 2) { //date
            if (input.length >= 5) { // 01.02.93, 1.02.93
                edited = insertAt(edited, ".", cursorStart - 4)
                cursorStart++
                edited = insertAt(edited, ".", cursorStart - 2)
                cursorStart++
                editText!!.setText(edited)
            } else if (input.length >= 3) { // 01.02, 1.02
                edited = insertAt(edited, ".", cursorStart - 2)
                cursorStart++
                editText!!.setText(edited)
            }
        } //number
        editText!!.setSelection(cursorStart, cursorStart)
        input = StringBuffer()
    }

    fun resetInput() {
        input = StringBuffer()
    }

    private fun hideAndBack() {
        isVisible = false
        listener!!.onNumKeyboardClosed()
    }
}