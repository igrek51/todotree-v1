package igrek.todotree.gui.numkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import igrek.todotree.R;

public class NumericKeyboardView extends KeyboardView implements KeyboardView.OnKeyboardActionListener {

    //TODO: zmiana kursora bez wpisania tekstu powoduje reset bufora

    private Context context;
    private EditText editText;

    NumKeyboardListener listener;

    private StringBuffer inputed = new StringBuffer();

    private int mode; //1 - godzina, 2 - data, 3 - liczba (waluta)
    
    public NumericKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public NumericKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void init(NumKeyboardListener listener, EditText editText) {
        this.editText = editText;
        this.listener = listener;
        Keyboard keyboard = new Keyboard(context, R.xml.numeric_keyboard);
        setKeyboard(keyboard);
        setVisibility(View.GONE);
        setEnabled(false);
        setPreviewEnabled(true);
        setOnKeyboardActionListener(this);
    }

    public boolean isVisible() {
        switch (getVisibility()) {
            case View.VISIBLE:
                return true;
            case View.GONE:
            case View.INVISIBLE:
                return false;
        }
        return false;
    }

    public void setVisible(boolean visible) {
        if (visible == isVisible()) {
            return;
        }
        if (visible) {
            setVisibility(View.VISIBLE);
            setEnabled(true);
        } else {
            setVisibility(View.GONE);
            setEnabled(false);
        }
    }

    @Override
    public void swipeDown() {
        hideAndBack();
    }

    @Override
    public void swipeRight() {
        super.swipeRight();
    }

    @Override
    public void swipeLeft() {
        super.swipeLeft();
    }

    @Override
    public void swipeUp() {
        super.swipeUp();
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode >= 48 && primaryCode <= 57) {
            typedNumber((char) primaryCode);
        } else if (primaryCode == 45) {
            typedMinus();
        } else if (primaryCode == 44) {
            typedComma();
        } else if (primaryCode == -6) {
            typedOK();
        } else if (primaryCode == -3) {
            typedBackspace();
        }
        //TODO pozostałe znaki: dwukropek, ...
        //TODO: złamanie trybu i zmiana na numeryczny po wpisaniu niewłaściwych znaków (lub poprawienie znaków na znaki trybu)
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    private void typedNumber(char c) {
        inputed.append(c);
        insertString("" + c);

        if (mode == 1) { //godzina
            if (inputed.length() >= 4) {
                finishTyping();
                hideAndBack();
            }
        } else if (mode == 2) { //data
            if (inputed.length() >= 6) {
                finishTyping();
                hideAndBack();
            }
        }
    }

    private void typedMinus() {
        insertString("-");
    }

    private void typedComma() {
        insertString(",");
    }

    private void typedOK() {
        finishTyping();
        hideAndBack();
    }

    private void typedBackspace() {
        if (inputed.length() > 0) {
            inputed.delete(inputed.length() - 1, inputed.length());
        }
        int selStart = editText.getSelectionStart();
        int selEnd = editText.getSelectionEnd();

        String edited = editText.getText().toString();

        String before = edited.substring(0, selStart);
        String after = edited.substring(selEnd);

        if (selStart == selEnd) {
            if (selStart > 0) {
                edited = before.substring(0, before.length() - 1) + after;
                editText.setText(edited);
                editText.setSelection(selStart - 1, selEnd - 1);
            }
        } else {
            edited = before + after;
            editText.setText(edited);
            editText.setSelection(selStart, selStart);
        }
    }


    private String insertAt(String str, String c, int offset) {
        if (offset < 0) offset = 0;
        if (offset > str.length()) offset = str.length();
        String before = str.substring(0, offset);
        String after = str.substring(offset);
        return before + c + after;
    }

    private void insertString(String str) {
        int selStart = editText.getSelectionStart();
        int selEnd = editText.getSelectionEnd();

        String edited = editText.getText().toString();

        String before = edited.substring(0, selStart);
        String after = edited.substring(selEnd);

        edited = before + str + after;
        editText.setText(edited);

        editText.setSelection(selStart + str.length(), selEnd + str.length());
    }

    public void startTyping(int mode) {
        this.mode = mode;
        inputed = new StringBuffer();
    }

    public void finishTyping() {
        int cursorStart = editText.getSelectionStart();
        int cursorEnd = editText.getSelectionEnd();
        String edited = editText.getText().toString();

        if (cursorStart != cursorEnd) return;

        if (mode == 1) { //godzina
            if (inputed.length() >= 3) { // 01:02, 1:02
                edited = insertAt(edited, ":", cursorStart - 2);
                cursorStart++;
                editText.setText(edited);
            }
        } else if (mode == 2) { //data
            if (inputed.length() >= 5) { // 01.02.93, 1.02.93
                edited = insertAt(edited, ".", cursorStart - 4);
                cursorStart++;
                edited = insertAt(edited, ".", cursorStart - 2);
                cursorStart++;
                editText.setText(edited);
            } else if (inputed.length() >= 3) { // 01.02, 1.02
                edited = insertAt(edited, ".", cursorStart - 2);
                cursorStart++;
                editText.setText(edited);
            }
        } else if (mode == 3) { //liczba lub waluta

        }
        editText.setSelection(cursorStart, cursorStart);
        inputed = new StringBuffer();
    }

    public void resetInputed() {
        inputed = new StringBuffer();
    }

    private void hideAndBack() {
        setVisible(false);
        listener.onNumKeyboardClosed();
    }
}
