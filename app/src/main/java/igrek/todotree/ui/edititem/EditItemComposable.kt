@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.todotree.ui.edititem

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igrek.todotree.R
import igrek.todotree.compose.md_theme_dark_surfaceVariant
import igrek.todotree.compose.md_theme_light_primaryContainer
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch


@Composable
internal fun MainComponent(controller: EditItemLayout) {
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
                        controller.numericTyper.closeNumericKeyboard()
                    }
                ),
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .fillMaxWidth()
                    .focusRequester(state.focusRequester)
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyUp) {
                            controller.numericTyper.onKeyUp(it.nativeKeyEvent.keyCode, it.nativeKeyEvent.number)
                        }
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
                        controller.numericTyper.toggleTypingHour()
                    },
                )
                FlatButton(
                    modifier = Modifier.weight(2f),
                    text = "dd.MM",
                    onClick = {
                        controller.numericTyper.toggleTypingDate()
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
                        controller.numericTyper.toggleTypingNumeric()
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
