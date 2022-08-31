package igrek.todotree.ui.edititem

import android.os.Handler
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView.OnEditorActionListener
import igrek.todotree.R
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.info.errorcheck.SafeClickListener
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.RemotePushCommand
import igrek.todotree.ui.GUI
import igrek.todotree.ui.numkeyboard.NumKeyboardListener
import igrek.todotree.ui.numkeyboard.NumericKeyboardView

class EditItemGUI(
    private val gui: GUI,
    item: AbstractTreeItem?,
    parent: AbstractTreeItem,
) : NumKeyboardListener {

    private var etEditItem: ItemEditText? = null
    private var buttonSaveItem: Button? = null
    private var numericKeyboard: NumericKeyboardView? = null

    private fun init(item: AbstractTreeItem?, parent: AbstractTreeItem) {
        val editItemContentLayout = gui.setMainContentLayout(R.layout.edit_item_content)
        etEditItem = editItemContentLayout.findViewById<View>(R.id.etEditItemContent) as ItemEditText
        buttonSaveItem = editItemContentLayout.findViewById<View>(R.id.buttonSaveItem) as Button
        val buttonSaveAndAdd = editItemContentLayout.findViewById<View>(R.id.buttonSaveAndAddItem) as Button
        val buttonSaveAndGoInto = editItemContentLayout.findViewById<View>(R.id.buttonSaveAndGoInto) as Button
        val rotateScreenBtn = editItemContentLayout.findViewById<View>(R.id.rotateScreenBtn) as ImageButton

        if (RemotePushCommand().isRemotePushingEnabled()) {
            buttonSaveAndAdd.visibility = View.GONE
            buttonSaveAndGoInto.visibility = View.GONE
        }

        gui.setTitle(parent.displayName)
        if (item != null) {
            etEditItem!!.setText(item.displayName)
            buttonSaveItem!!.setOnClickListener(SafeClickListener {
                if (numericKeyboard!!.isVisible) {
                    numericKeyboard!!.finishTyping()
                }
                hideKeyboards()
                ItemEditorCommand().saveItem(item, etEditItem!!.text.toString())
            })
            buttonSaveAndAdd.setOnClickListener(SafeClickListener {
                ItemEditorCommand().saveAndAddItemClicked(item, etEditItem!!.text
                        .toString())
                hideKeyboards()
            })
            buttonSaveAndGoInto.setOnClickListener(SafeClickListener {
                ItemEditorCommand().saveAndGoIntoItemClicked(item, etEditItem!!.text
                    .toString())
                hideKeyboards()
            })
        } else {
            etEditItem!!.setText("")
            buttonSaveItem!!.setOnClickListener(SafeClickListener {
                if (numericKeyboard!!.isVisible) {
                    numericKeyboard!!.finishTyping()
                }
                hideKeyboards()
                ItemEditorCommand().saveItem(null, etEditItem!!.text.toString())
            })
            buttonSaveAndAdd.setOnClickListener(SafeClickListener {
                ItemEditorCommand().saveAndAddItemClicked(null, etEditItem!!.text
                        .toString())
                hideKeyboards()
            })
            buttonSaveAndGoInto.setOnClickListener(SafeClickListener {
                ItemEditorCommand().saveAndGoIntoItemClicked(null, etEditItem!!.text
                        .toString())
                hideKeyboards()
            })
        }
        rotateScreenBtn.setOnClickListener(SafeClickListener {
            gui.rotateScreen()
        })

        val buttonEditCancel = editItemContentLayout.findViewById<View>(R.id.buttonEditCancel) as Button
        buttonEditCancel.setOnClickListener(SafeClickListener {
            ItemEditorCommand().cancelEditedItem()
            hideKeyboards()
        })

        val quickEditGoBegin = editItemContentLayout.findViewById<View>(R.id.quickEditGoBegin) as ImageButton
        quickEditGoBegin.setOnClickListener(SafeClickListener {
            quickCursorMove(-2)
        })
        val quickEditGoLeft = editItemContentLayout.findViewById<View>(R.id.quickEditGoLeft) as ImageButton
        quickEditGoLeft.setOnClickListener(SafeClickListener {
            quickCursorMove(-1)
        })
        val quickEditSelectAll = editItemContentLayout.findViewById<View>(R.id.quickEditSelectAll) as ImageButton
        quickEditSelectAll.setOnClickListener(SafeClickListener {
            quickEditSelectAll()
        })
        val quickEditGoRight = editItemContentLayout.findViewById<View>(R.id.quickEditGoRight) as ImageButton
        quickEditGoRight.setOnClickListener(SafeClickListener {
            quickCursorMove(+1)
        })
        val quickEditGoEnd = editItemContentLayout.findViewById<View>(R.id.quickEditGoEnd) as ImageButton
        quickEditGoEnd.setOnClickListener(SafeClickListener {
            quickCursorMove(+2)
        })

        val buttonEditInsertTime = editItemContentLayout.findViewById<View>(R.id.buttonEditInsertTime) as Button
        buttonEditInsertTime.setOnClickListener(SafeClickListener {
            toggleTypingHour()
        })

        val buttonEditInsertDate = editItemContentLayout.findViewById<View>(R.id.buttonEditInsertDate) as Button
        buttonEditInsertDate.setOnClickListener(SafeClickListener {
            toggleTypingDate()
        })

        val buttonEditInsertNumber = editItemContentLayout.findViewById<View>(R.id.buttonEditInsertNumber) as Button
        buttonEditInsertNumber.setOnClickListener(SafeClickListener {
            toggleTypingNumeric()
        })

        val buttonEditInsertRange = editItemContentLayout.findViewById<View>(R.id.buttonEditInsertRange) as Button
        buttonEditInsertRange.setOnClickListener(SafeClickListener {
            quickInsertRange()
            numericKeyboard!!.resetInput()
        })
        etEditItem!!.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buttonSaveItem!!.performClick()
                return@OnEditorActionListener true
            }
            false
        })
        etEditItem!!.setNumKeyboardListener(this)

        etEditItem!!.setOnClickListener(SafeClickListener {
            if (numericKeyboard!!.isVisible) {
                showNumericKeyboard()
                numericKeyboard!!.resetInput()
            } else {
                showAlphanumKeyboard()
            }
        })
        numericKeyboard = editItemContentLayout.findViewById<View>(R.id.numericKeyboard) as NumericKeyboardView
        numericKeyboard!!.init(this, etEditItem)

        etEditItem!!.requestFocus()
        quickCursorMove(+2)

        // delayed execution due to not showing keyboard after action select
        forceKeyboardShow()
    }

    fun forceKeyboardShow() {
        Handler().post { showAlphanumKeyboard() }
    }

    private fun quickCursorMove(direction: Int) {
        if (direction == -2) { //to the beginning
            etEditItem!!.requestFocus()
            etEditItem!!.selectAll()
            etEditItem!!.setSelection(0)
            Handler().post { etEditItem!!.setSelection(0) }
        } else if (direction == +2) {
            etEditItem!!.setSelection(etEditItem!!.text!!.length)
        } else if (direction == -1 || direction == +1) {
            var selStart = etEditItem!!.selectionStart
            var selEnd = etEditItem!!.selectionEnd
            if (selStart == selEnd) {
                selStart += direction
                if (selStart < 0) selStart = 0
                if (selStart > etEditItem!!.text!!.length) selStart = etEditItem!!.text!!.length
                etEditItem!!.setSelection(selStart)
            } else {
                if (direction == -1) { //left
                    selStart--
                    if (selStart < 0) selStart = 0
                } else { //right
                    selEnd++
                    if (selEnd > etEditItem!!.text!!.length) selEnd = etEditItem!!.text!!.length
                }
                etEditItem!!.setSelection(selStart, selEnd)
            }
        }
    }

    private fun quickEditSelectAll() {
        etEditItem!!.requestFocus()
        etEditItem!!.selectAll()
        Handler().post { etEditItem!!.selectAll() }
    }

    fun quickInsertRange() {
        if (numericKeyboard!!.isVisible) {
            numericKeyboard!!.finishTyping()
        }
        var edited = etEditItem!!.text.toString()
        var selStart = etEditItem!!.selectionStart
        val selEnd = etEditItem!!.selectionEnd
        val before = edited.substring(0, selStart)
        val after = edited.substring(selEnd)

        if (before.isNotEmpty() && before[before.length - 1] == ' ') {
            edited = "$before- $after"
            selStart += 2
        } else {
            edited = "$before - $after"
            selStart += 3
        }
        etEditItem!!.setText(edited)
        etEditItem!!.setSelection(selStart, selStart)
    }

    private fun showAlphanumKeyboard() {
        val selEnd = etEditItem!!.selectionEnd
        val selStart = etEditItem!!.selectionStart
        numericKeyboard!!.isVisible = false
        gui.showSoftKeyboard(etEditItem)
        etEditItem!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        etEditItem!!.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_DONE
        etEditItem!!.setSelection(selStart, selEnd)
    }

    private fun showNumericKeyboard() {
        val selEnd = etEditItem!!.selectionEnd
        val selStart = etEditItem!!.selectionStart
        gui.hideSoftKeyboard(etEditItem!!)
        numericKeyboard!!.isVisible = true
        etEditItem!!.setSelection(selStart, selEnd)
    }

    fun hideKeyboards() {
        val selEnd = etEditItem!!.selectionEnd
        val selStart = etEditItem!!.selectionStart
        gui.hideSoftKeyboard(etEditItem!!)
        numericKeyboard!!.isVisible = false
        etEditItem!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        etEditItem!!.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_DONE
        etEditItem!!.setSelection(selStart, selEnd)
    }

    private fun toggleTypingHour() {
        toggleTyping(1)
    }

    private fun toggleTypingDate() {
        toggleTyping(2)
    }

    private fun toggleTypingNumeric() {
        toggleTyping(3)
    }

    private fun toggleTyping(mode: Int) {
        if (numericKeyboard!!.isVisible && numericKeyboard!!.typingMode == mode) {
            showAlphanumKeyboard()
        } else {
            showNumericKeyboard()
            numericKeyboard!!.startTyping(mode)
        }
    }

    override fun onNumKeyboardClosed() {
        showAlphanumKeyboard()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {}

    fun editItemBackClicked(): Boolean {
        hideKeyboards()
        return numericKeyboard!!.isVisible
    }

    fun requestSaveEditedItem() {
        buttonSaveItem!!.performClick()
    }

    init {
        init(item, parent)
    }
}