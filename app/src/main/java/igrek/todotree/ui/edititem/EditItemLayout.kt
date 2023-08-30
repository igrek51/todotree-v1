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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.md_theme_dark_surfaceVariant
import igrek.todotree.compose.md_theme_light_primaryContainer
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ItemEditorCommand
import igrek.todotree.intent.RemotePushCommand
import igrek.todotree.service.system.SoftKeyboardService
import igrek.todotree.ui.GUI
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch


class EditItemLayout {
    private val gui: GUI by LazyExtractor(appFactory.gui)
    private val softKeyboardService: SoftKeyboardService by LazyExtractor(appFactory.softKeyboardService)

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
        state.selectionMode.value = false
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

    private fun isSelecting(): Boolean {
        val selStart = state.textFieldValue.value.selection.start
        val selEnd = state.textFieldValue.value.selection.end
        return selEnd > selStart || state.selectionMode.value
    }

    fun quickCursorMove(direction: Int) {
        val text = state.textFieldValue.value.text
        var selStart = state.textFieldValue.value.selection.start
        var selEnd = state.textFieldValue.value.selection.end
        when (isSelecting()) {
            true -> { // expand selection
                if (direction == -1) {
                    selStart--
                    if (selStart < 0) selStart = 0
                } else {
                    selEnd++
                    if (selEnd > text.length) selEnd = text.length
                }
                setSelection(selStart, selEnd)
            }
            else -> { // no selection, move
                selStart += direction
                if (selStart < 0) selStart = 0
                if (selStart > text.length) selStart = text.length
                setSelection(selStart)
            }
        }
    }

    fun quickCursorJump(direction: Int) {
        val text = state.textFieldValue.value.text
        var selStart = state.textFieldValue.value.selection.start
        var selEnd = state.textFieldValue.value.selection.end
        when (isSelecting()) {
            true -> { // expand selection
                if (direction == -1) {
                    selStart = 0
                } else {
                    selEnd = text.length
                }
                setSelection(selStart, selEnd)
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

    fun quickEditSelect() {
        state.selectionMode.value = !isSelecting()
        if (!state.selectionMode.value) {
            val selStart = state.textFieldValue.value.selection.start
            val selEnd = state.textFieldValue.value.selection.end
            if (selStart < selEnd) {
                setSelection(selStart)
            }
        }
    }

    fun selectAllText() {
        val text = state.textFieldValue.value.text
        setSelection(0, text.length)
    }

    fun onCopyClick() {

    }

    fun onPasteClick() {

    }

    fun onBackspaceClick() {

    }

    fun onReverseBackspaceClick() {

    }

    fun toggleTypingHour() {
    }

    fun toggleTypingDate() {
    }

    fun toggleTypingNumeric() {
    }

    fun quickInsertRange() {
    }

    fun quickInsertColon() {
    }


}


class EditItemState {
    val textFieldValue: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue(text = ""))
    val remotePushingEnabled: MutableState<Boolean> = mutableStateOf(false)
    val existingItem: MutableState<Boolean> = mutableStateOf(false)
    val selectionMode: MutableState<Boolean> = mutableStateOf(false)
    val focusRequester: FocusRequester = FocusRequester()
}


@Composable
private fun MainComponent(controller: EditItemLayout) {
    LoggerFactory.logger.debug("recomposition of EditItemLayout Main")
    val state = controller.state
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Column {
            OutlinedTextField(
                value = state.textFieldValue.value,
                onValueChange = { state.textFieldValue.value = it },
                label = null,
                singleLine = false,
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester),
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
                MyFlatIconButton(
                    drawableResId = R.drawable.selection,
                    onClick = {
                        controller.quickEditSelect()
                    },
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.select_all,
                    onClick = {
                        controller.selectAllText()
                    },
                )
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
                    text = "dd.MM.yy",
                    onClick = {
                        controller.toggleTypingDate()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = "-",
                    onClick = {
                        controller.quickInsertRange()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = ":",
                    onClick = {
                        controller.quickInsertColon()
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

@Preview
@Composable
fun ComposablePreview() {
    MainComponent(EditItemLayout())
}
