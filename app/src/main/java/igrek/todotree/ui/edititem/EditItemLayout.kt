@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.todotree.ui.edititem

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.md_theme_dark_surfaceVariant
import igrek.todotree.compose.md_theme_light_primaryContainer
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ClipboardCommand
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.RemotePushCommand
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.ui.GUI
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch


class EditItemLayout {
    private val gui: GUI by LazyExtractor(appFactory.gui)
    private val softKeyboardService: SoftKeyboardService by LazyExtractor(appFactory.softKeyboardService)
    private val uiInfoService: UiInfoService by LazyExtractor(appFactory.uiInfoService)
    private val systemClipboardManager: SystemClipboardManager by LazyExtractor(appFactory.systemClipboardManager)

    private var currentItem: AbstractTreeItem? = null
    private var parent: AbstractTreeItem = RootTreeItem()

    val state = EditItemState()

    fun setCurrentItem(currentItem: AbstractTreeItem?, parent: AbstractTreeItem) {
        this.currentItem = currentItem
        this.parent = parent
    }

    fun showLayout(layout: View) {
        resetState()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view_edit).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

        state.focusRequester.requestFocus()

        gui.setTitle(parent.displayName)

        showKeyboard()
    }

    private fun resetState() {
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

        state.remotePushingEnabled.value = RemotePushCommand().isRemotePushingEnabled()
        state.manualSelectionMode.value = false
        state.numericKeyboard.value = false
        state.typingHour.value = false
        state.typingDate.value = false
    }

    fun onSaveItemClick() {
        hideKeyboards()
        currentItem?.let { item ->
            ItemEditorCommand().saveItem(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveItem(null, state.textFieldValue.value.text)
        }
    }

    fun onSaveAndAddClick() {
        hideKeyboards()
        currentItem?.let { item ->
            ItemEditorCommand().saveAndAddItemClicked(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveAndAddItemClicked(null, state.textFieldValue.value.text)
        }
    }

    fun onSaveAndEnterClick() {
        hideKeyboards()
        currentItem?.let { item ->
            ItemEditorCommand().saveAndGoIntoItemClicked(item, state.textFieldValue.value.text)
        } ?: run {
            ItemEditorCommand().saveAndGoIntoItemClicked(null, state.textFieldValue.value.text)
        }
    }

    fun onCancelClick() {
        hideKeyboards()
        ItemEditorCommand().cancelEditedItem()
    }

    fun hideKeyboards() {
        state.focusRequester.freeFocus()
        softKeyboardService.hideSoftKeyboard()
    }

    fun showKeyboard() {
        softKeyboardService.showSoftKeyboard()
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
        state.numericKeyboard.value = !state.numericKeyboard.value
        state.numericBuffer.value = ""
    }

    fun closeNumericKeyboard() {
        finishNumericTyping()
        state.numericKeyboard.value = false
        state.typingHour.value = false
        state.typingDate.value = false
    }

    fun onKeyUp(key: Char) {
        when (key) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                state.numericBuffer.value += key
                checkNumericTypingBuffer()
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

    private fun finishNumericTyping() {
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
            }
            state.typingDate.value && buffer.length >= 3 -> { // date 01.02 or 1.02
                val edited = text.insertAt(".", cursor - 2)
                cursor += 1
                state.textFieldValue.value = TextFieldValue(
                    text = edited,
                    selection = TextRange(cursor, cursor),
                )
            }
        }
        state.numericBuffer.value = ""
    }

    fun insertHyphen() {
        finishNumericTyping()
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
        finishNumericTyping()
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
        hideKeyboards()
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


class EditItemState {
    val textFieldValue: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue(text = ""))
    val remotePushingEnabled: MutableState<Boolean> = mutableStateOf(false)
    val existingItem: MutableState<Boolean> = mutableStateOf(false)
    val manualSelectionMode: MutableState<Boolean> = mutableStateOf(false)
    val numericKeyboard: MutableState<Boolean> = mutableStateOf(false)
    val numericBuffer: MutableState<String> = mutableStateOf("")
    val typingHour: MutableState<Boolean> = mutableStateOf(false)
    val typingDate: MutableState<Boolean> = mutableStateOf(false)
    val focusRequester: FocusRequester = FocusRequester()
}


@Composable
private fun MainComponent(controller: EditItemLayout) {
    LoggerFactory.logger.debug("recomposition: EditItemLayout Main")
    val state = controller.state
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Column {
            val keyboardOptions: KeyboardOptions = when (state.numericKeyboard.value) {
                true -> KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                false -> KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Default)
            }
            OutlinedTextField(
                value = state.textFieldValue.value,
                onValueChange = {
                    controller.onTextChange(it)
                },
                label = null,
                singleLine = false,
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(
                    onDone = {
                        controller.closeNumericKeyboard()
                    }
                ),
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester)
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyUp) {
                            controller.onKeyUp(it.nativeKeyEvent.number)
                        }
                        LoggerFactory.logger.debug("keycode: ${it.type}, ${it.nativeKeyEvent.keyCode}, ${it.nativeKeyEvent.number}")
                        false
                    },
                textStyle = TextStyle.Default.copy(fontSize = 16.sp),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FlatButton(
                    modifier = Modifier.weight(0.5f),
                    iconVector = Icons.Filled.Done,
                    text = "Save",
                    onClick = {
                        controller.onSaveItemClick()
                    },
                )

                if (!controller.state.remotePushingEnabled.value) {
                    FlatButton(
                        modifier = Modifier.weight(0.25f),
                        iconVector = Icons.Filled.Done,
                        text = "& add",
                        onClick = {
                            controller.onSaveAndAddClick()
                        },
                    )
                    FlatButton(
                        modifier = Modifier.weight(0.25f),
                        iconVector = Icons.Filled.Done,
                        text = "& enter",
                        onClick = {
                            controller.onSaveAndEnterClick()
                        },
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MyFlatIconButton(
                    drawableResId = R.drawable.navigate_previous,
                    onClick = {
                        controller.quickCursorJump(-1)
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.arrow_left,
                    onClick = {
                        controller.quickCursorMove(-1)
                    },
                )
                ToggleSelectionButton(controller)
                MyFlatIconButton(
                    drawableResId = R.drawable.arrow_right,
                    onClick = {
                        controller.quickCursorMove(+1)
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.navigate_next,
                    onClick = {
                        controller.quickCursorJump(+1)
                    },
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MyFlatIconButton(
                    modifier = Modifier,
                    drawableResId = R.drawable.copy,
                    onClick = {
                        controller.onCopyClick()
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.paste,
                    onClick = {
                        controller.onPasteClick()
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.select_all,
                    onClick = {
                        controller.selectAllText()
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.backspace,
                    onClick = {
                        controller.onBackspaceClick()
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.backspace_reverse,
                    onClick = {
                        controller.onReverseBackspaceClick()
                    },
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FlatButton(
                    modifier = Modifier.weight(2f),
                    text = "HH:mm",
                    onClick = {
                        controller.toggleTypingHour()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(2f),
                    text = "dd.MM",
                    onClick = {
                        controller.toggleTypingDate()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = "-",
                    onClick = {
                        controller.insertHyphen()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = ":",
                    onClick = {
                        controller.insertColon()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(1.5f),
                    text = "123",
                    onClick = {
                        controller.toggleTypingNumeric()
                    },
                )
            }

            Row {
                FlatButton(
                    modifier = Modifier.fillMaxWidth(),
                    iconVector = Icons.Filled.Close,
                    text = "Cancel",
                    onClick = {
                        controller.onCancelClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun FlatButton(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier
            .padding(vertical = 2.dp)
            .heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = md_theme_dark_surfaceVariant,
            contentColor = Color.White
        ),
    ) {
        if (iconVector != null) {
            Icon(
                iconVector,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(text)
    }
}

@Composable
private fun (RowScope).MyFlatIconButton(
    @DrawableRes drawableResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier
            .weight(1f)
            .padding(vertical = 2.dp)
            .heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 0.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = md_theme_dark_surfaceVariant,
            contentColor = Color.White
        ),
    ) {
        Icon(
            painterResource(id = drawableResId),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color.White,
        )
    }
}

@Composable
private fun (RowScope).ToggleSelectionButton(
    controller: EditItemLayout,
) {
    val isSelecting: Boolean by remember {
        derivedStateOf {
            controller.isSelecting()
        }
    }
    val drawableResId = when (isSelecting) {
        true -> R.drawable.selection_remove
        false -> R.drawable.selection
    }
    MyFlatIconButton(
        drawableResId = drawableResId,
        onClick = {
            controller.toggleSelectionMode()
        },
    )
}

@Preview
@Composable
fun ComposablePreview() {
    MainComponent(EditItemLayout())
}

private fun TextRange.isDifferent(other: TextRange): Boolean {
    return this.start != other.start || this.end != other.end
}

private fun String.insertAt(c: String, offset: Int): String {
    var mOffset = offset
    if (mOffset < 0) mOffset = 0
    if (mOffset > this.length) mOffset = this.length
    val before = this.substring(0, mOffset)
    val after = this.substring(mOffset)
    return before + c + after
}
