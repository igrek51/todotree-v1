package igrek.todotree.ui.edititem

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.errorcheck.UiErrorHandler
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ClipboardCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.ui.GUI


class EditItemLayout {
    private val gui: GUI by LazyExtractor(appFactory.gui)
    private val softKeyboardService: SoftKeyboardService by LazyExtractor(appFactory.softKeyboardService)
    private val uiInfoService: UiInfoService by LazyExtractor(appFactory.uiInfoService)
    private val systemClipboardManager: SystemClipboardManager by LazyExtractor(appFactory.systemClipboardManager)

    private var currentItem: AbstractTreeItem? = null
    private var parent: AbstractTreeItem = RootTreeItem()
    private var wired: Boolean = false
    val state = EditItemState()
    val numericTyper = NumericTyper(state)

    fun setCurrentItem(currentItem: AbstractTreeItem?, parent: AbstractTreeItem) {
        this.currentItem = currentItem
        this.parent = parent
    }

    fun showCachedLayout(layout: View) {
        when (wired) {
            false -> {
                initLayout(layout)
                wired = true
            }
            true -> {
                updateState()
                postLayoutUpdate()
            }
        }
    }

    private fun initLayout(layout: View) {
        updateState()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view_edit).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

        postLayoutUpdate()
    }

    private fun postLayoutUpdate() {
        try {
            state.focusRequester.requestFocus()
        } catch (t: Exception) {
            UiErrorHandler.handleError(t)
        }

        gui.setTitle(parent.displayName)
        showKeyboard()
        gui.stopLoading()
    }

    private fun updateState() {
        currentItem?.let { item ->
            val text = item.displayName
            state.textFieldValue.value = TextFieldValue(
                text = text,
                selection = TextRange(start = text.length, end = text.length) // cursor at the end
            )
            state.existingItem.value = true
        } ?: run {
            state.textFieldValue.value = TextFieldValue("")
            state.existingItem.value = false
        }

        state.manualSelectionMode.value = false
        state.numericKeyboard.value = false
        state.typingHour.value = false
        state.typingDate.value = false
        state.startEditTimestamp.value = System.currentTimeMillis()
    }

    fun onSaveItemClick() {
        hideKeyboard()
        currentItem?.let { item ->
            ItemEditorCommand().saveItem(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveItem(null, state.textFieldValue.value.text)
        }
    }

    fun onSaveAndAddClick() {
        currentItem?.let { item ->
            ItemEditorCommand().saveAndAddItemClicked(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveAndAddItemClicked(null, state.textFieldValue.value.text)
        }
    }

    fun onSaveAndEnterClick() {
        currentItem?.let { item ->
            ItemEditorCommand().saveAndGoIntoItemClicked(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveAndGoIntoItemClicked(null, state.textFieldValue.value.text)
        }
    }

    fun onCancelClick() {
        hideKeyboard()
        ItemEditorCommand().cancelEditedItem()
    }

    fun showKeyboard() {
        softKeyboardService.showSoftKeyboard()
    }

    fun hideKeyboard() {
        state.focusRequester.freeFocus()
        softKeyboardService.hideSoftKeyboard()
    }

    fun isSelecting(): Boolean {
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        return selMin < selMax || state.manualSelectionMode.value
    }

    fun quickCursorMove(direction: Int) {
        val text = state.textFieldValue.value.text
        val selStart = state.textFieldValue.value.selection.start
        val selEnd = state.textFieldValue.value.selection.end
        when (isSelecting()) {
            true -> { // expand selection
                var newSelEnd = selEnd
                if (direction == -1) {
                    newSelEnd--
                    if (newSelEnd < 0) newSelEnd = 0
                } else {
                    newSelEnd++
                    if (newSelEnd > text.length) newSelEnd = text.length
                }
                if (newSelEnd != selEnd) {
                    setSelection(selStart, newSelEnd)
                } else {
                    setSelection(newSelEnd)
                }
            }
            else -> { // no selection, move
                var cursor = selStart + direction
                if (cursor < 0) cursor = 0
                if (cursor > text.length) cursor = text.length
                setSelection(cursor)
            }
        }
    }

    fun quickCursorJump(direction: Int) {
        val text = state.textFieldValue.value.text
        val selStart = state.textFieldValue.value.selection.start
        val selEnd = state.textFieldValue.value.selection.end
        when (isSelecting()) {
            true -> { // is actively selecting - expand selection
                val newSelEnd = if (direction == -1) {
                    0
                } else {
                    text.length
                }
                if (newSelEnd != selEnd) {
                    setSelection(selStart, newSelEnd)
                } else {
                    setSelection(newSelEnd)
                }
            }
            else -> { // no selection, jump
                if (direction == -1) { // jump to the beginning
                    setSelection(0)
                } else if (direction == +1) { // jump to the end
                    setSelection(text.length)
                }
            }
        }
    }

    private fun setSelection(start: Int, end: Int? = null) {
        val mEnd = end ?: start
        state.textFieldValue.value = state.textFieldValue.value
            .copy(selection = TextRange(start, mEnd))
    }

    fun toggleSelectionMode() {
        state.manualSelectionMode.value = !isSelecting()
        if (!state.manualSelectionMode.value) {
            val selStart = state.textFieldValue.value.selection.start
            val selEnd = state.textFieldValue.value.selection.end
            if (selStart != selEnd) { // cancel selection
                setSelection(selEnd)
            }
        }
    }

    fun selectAllText() {
        val text = state.textFieldValue.value.text
        setSelection(0, text.length)
        state.manualSelectionMode.value = false
    }

    fun onCopyClick() {
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        if (selMin == selMax) {
            uiInfoService.showInfo("No text selected")
            return
        }
        val selectedText = text.substring(selMin, selMax)
        ClipboardCommand().copyAsText(selectedText)
    }

    fun onPasteClick() {
        val clipboardText = systemClipboardManager.systemClipboard.takeIf { !it.isNullOrBlank() }
            ?: return run {
                uiInfoService.showInfo("Clipboard is empty")
            }
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        val newText = text.substring(0, selMin) + clipboardText + text.substring(selMax)
        val position = selMax + clipboardText.length
        state.textFieldValue.value = TextFieldValue(
            text = newText,
            selection = TextRange(position, position),
        )
    }

    fun onBackspaceClick() {
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        when {
            selMin != selMax -> { // erase selection
                val newText = text.substring(0, selMin) + text.substring(selMax)
                state.textFieldValue.value = TextFieldValue(
                    text = newText,
                    selection = TextRange(selMin, selMin),
                )
            }
            selMin > 0 -> {
                val newText = text.substring(0, selMin - 1) + text.substring(selMin)
                state.textFieldValue.value = TextFieldValue(
                    text = newText,
                    selection = TextRange(selMin - 1, selMin - 1),
                )
            }
        }
    }

    fun onReverseBackspaceClick() {
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        when {
            selMin != selMax -> { // erase selection
                val newText = text.substring(0, selMin) + text.substring(selMax)
                state.textFieldValue.value = TextFieldValue(
                    text = newText,
                    selection = TextRange(selMin, selMin),
                )
            }
            selMin < text.length -> {
                val newText = text.substring(0, selMin) + text.substring(selMin + 1)
                state.textFieldValue.value = TextFieldValue(
                    text = newText,
                    selection = TextRange(selMin, selMin),
                )
            }
        }
    }

    fun insertHyphen() {
        numericTyper.finishNumericTyping()
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        val before = text.substring(0, selMin)
        val after = text.substring(selMax)
        var position = selMin
        val inserted1 = when {
            before.isNotEmpty() && before.last() == ' ' -> ""
            else -> " "
        }
        val inserted2 = when {
            after.isNotEmpty() && after.first() == ' ' -> ""
            else -> " "
        }
        val inserted = "$inserted1-$inserted2"
        position += inserted.length
        state.textFieldValue.value = TextFieldValue(
            text = before + inserted + after,
            selection = TextRange(position, position),
        )
    }

    fun insertColon() {
        numericTyper.finishNumericTyping()
        val text = state.textFieldValue.value.text
        val selMin = state.textFieldValue.value.selection.min
        val selMax = state.textFieldValue.value.selection.max
        val before = text.substring(0, selMin)
        val after = text.substring(selMax)
        var position = selMin
        val insertedAfter = when {
            state.numericKeyboard.value -> ""
            after.isNotEmpty() && after.first() == ' ' -> ""
            else -> " "
        }
        val inserted = ":$insertedAfter"
        position += inserted.length
        state.textFieldValue.value = TextFieldValue(
            text = before + inserted + after,
            selection = TextRange(position, position),
        )
    }

    fun onEditBackClicked(): Boolean {
        hideKeyboard()
        return false
    }

    fun onTextChange(change: TextFieldValue) {
        val currentSelection = state.textFieldValue.value.selection
        if (change.selection.isDifferent(currentSelection)) { // perhaps clicked manually somewehere else
            state.manualSelectionMode.value = false
        }
        // prevent from reversing selection for no reason
        if (change.selection.start != change.selection.end &&
            change.selection.start == currentSelection.end &&
            change.selection.end == currentSelection.start) {
            state.textFieldValue.value = change.copy(selection = TextRange(currentSelection.start, currentSelection.end))
        } else {
            state.textFieldValue.value = change
        }
    }
}

private fun TextRange.isDifferent(other: TextRange): Boolean {
    return this.start != other.start || this.end != other.end
}
