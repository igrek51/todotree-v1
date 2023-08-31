package igrek.todotree.ui.edititem

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue


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
