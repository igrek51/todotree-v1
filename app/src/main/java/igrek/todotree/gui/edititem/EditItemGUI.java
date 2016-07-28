package igrek.todotree.gui.edititem;

import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.numkeyboard.NumKeyboardListener;
import igrek.todotree.gui.numkeyboard.NumericKeyboardView;
import igrek.todotree.logic.datatree.TreeItem;

//TODO: przycisk zakresu na klawiaturze numerycznej

public class EditItemGUI implements NumKeyboardListener {

    private GUI gui;

    private ItemEditText etEditItem;
    private Button buttonSaveItem;
    NumericKeyboardView numericKeyboard;

    public EditItemGUI(GUI gui, final TreeItem item, TreeItem parent) {
        this.gui = gui;
        init(item, parent);
    }

    public EditText getEtEditItem() {
        return etEditItem;
    }

    public void init(final TreeItem item, TreeItem parent) {
        View editItemContentLayout = gui.setMainContentLayout(R.layout.edit_item_content);

        etEditItem = (ItemEditText) editItemContentLayout.findViewById(R.id.etEditItemContent);
        //przycisk zapisu
        buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.buttonSaveItem);
        //przycisk zapisz i dodaj nowy
        Button buttonSaveAndAdd = (Button) editItemContentLayout.findViewById(R.id.buttonSaveAndAddItem);

        gui.setTitle(parent.getContent());

        if (item != null) { //edycja
            etEditItem.setText(item.getContent());
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (numericKeyboard.isVisible()) {
                        numericKeyboard.finishTyping();
                    }
                    hideKeyboards();
                    gui.getGuiListener().onSavedEditedItem(item, etEditItem.getText().toString());
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(item, etEditItem.getText().toString());
                    hideKeyboards();
                }
            });
        } else { //nowy element
            etEditItem.setText("");
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSavedNewItem(etEditItem.getText().toString());
                    hideKeyboards();
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(null, etEditItem.getText().toString());
                    hideKeyboards();
                }
            });
        }

        //przycisk anuluj
        Button buttonEditCancel = (Button) editItemContentLayout.findViewById(R.id.buttonEditCancel);
        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gui.getGuiListener().onCancelEditedItem(item);
                hideKeyboards();
            }
        });

        //przyciski zmiany kursora i zaznaczenia
        ImageButton quickEditGoBegin = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoBegin);
        quickEditGoBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickCursorMove(-2);
            }
        });
        ImageButton quickEditGoLeft = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoLeft);
        quickEditGoLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickCursorMove(-1);
            }
        });
        ImageButton quickEditSelectAll = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditSelectAll);
        quickEditSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEditSelectAll();
            }
        });
        ImageButton quickEditGoRight = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoRight);
        quickEditGoRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickCursorMove(+1);
            }
        });
        ImageButton quickEditGoEnd = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoEnd);
        quickEditGoEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickCursorMove(+2);
            }
        });

        //klawiatura dodawania godziny
        Button buttonEditInsertTime = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertTime);
        buttonEditInsertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTypingHour();
            }
        });

        //klawiatura dodawania daty
        Button buttonEditInsertDate = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertDate);
        buttonEditInsertDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTypingDate();
            }
        });

        //numer
        Button buttonEditInsertNumber = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertNumber);
        buttonEditInsertNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTypingNumeric();
            }
        });

        //dodawanie przedziału
        Button buttonEditInsertRange = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertRange);
        buttonEditInsertRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickInsertRange();
                numericKeyboard.resetInput();
            }
        });

        etEditItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //zapis przyciskiem OK
                    buttonSaveItem.performClick();
                    return true;
                }
                return false;
            }
        });

        etEditItem.setNumKeyboardListener(this);

        //niepotrzebne na focus: etEditItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        etEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numericKeyboard.isVisible()) {
                    showNumericKeyboard();
                    numericKeyboard.resetInput();
                } else {
                    showAlphanumKeyboard();
                }
            }
        });

        numericKeyboard = (NumericKeyboardView) editItemContentLayout.findViewById(R.id.numericKeyboard);
        numericKeyboard.init(this, etEditItem);

        //focus na końcu edytowanego tekstu
        etEditItem.requestFocus();
        quickCursorMove(+2);

        showAlphanumKeyboard();
    }


    private void quickCursorMove(int direction) {
        if (direction == -2) { //na początek
            etEditItem.setSelection(0);
        } else if (direction == +2) { //na koniec
            etEditItem.setSelection(etEditItem.getText().length());
        } else if (direction == -1 || direction == +1) {
            int selStart = etEditItem.getSelectionStart();
            int selEnd = etEditItem.getSelectionEnd();
            if (selStart == selEnd) { //brak zaznaczenia
                selStart += direction;
                if (selStart < 0) selStart = 0;
                if (selStart > etEditItem.getText().length())
                    selStart = etEditItem.getText().length();
                etEditItem.setSelection(selStart);
            } else { //zaznaczenie wielu znaków
                //poszerzenie zaznaczenia
                if (direction == -1) { //w lewo
                    selStart--;
                    if (selStart < 0) selStart = 0;
                } else { //w prawo
                    selEnd++;
                    if (selEnd > etEditItem.getText().length())
                        selEnd = etEditItem.getText().length();
                }
                etEditItem.setSelection(selStart, selEnd);
            }
        }
    }

    private void quickEditSelectAll() {
        etEditItem.requestFocus();
        etEditItem.selectAll();
        // konieczne, żeby zaznaczyć cały tekst, a nie tylko przejść z kursorem na początek - WTF ???
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                etEditItem.selectAll();
            }
        });
    }


    private void quickInsertRange() {
        if (numericKeyboard.isVisible()) {
            numericKeyboard.finishTyping();
        }
        String edited = etEditItem.getText().toString();
        int selStart = etEditItem.getSelectionStart();
        int selEnd = etEditItem.getSelectionEnd();
        String before = edited.substring(0, selStart);
        String after = edited.substring(selEnd);
        //bez podwójnej spacji przed "-"
        if (before.length() > 0 && before.charAt(before.length() - 1) == ' ') {
            edited = before + "- " + after;
            selStart += 2;
        } else {
            edited = before + " - " + after;
            selStart += 3;
        }
        etEditItem.setText(edited);
        etEditItem.setSelection(selStart, selStart);
    }

    private void showAlphanumKeyboard() {
        int selEnd = etEditItem.getSelectionEnd();
        int selStart = etEditItem.getSelectionStart();

        numericKeyboard.setVisible(false);
        gui.showSoftKeyboard(etEditItem);

        etEditItem.setInputType(InputType.TYPE_CLASS_TEXT);
        etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);

        etEditItem.setSelection(selStart, selEnd);
    }

    private void showNumericKeyboard() {
        int selEnd = etEditItem.getSelectionEnd();
        int selStart = etEditItem.getSelectionStart();

        gui.hideSoftKeyboard(etEditItem);
        numericKeyboard.setVisible(true);

        //        etEditItem.setInputType(InputType.TYPE_NULL);

        etEditItem.setSelection(selStart, selEnd);
    }

    public void hideKeyboards() {
        int selEnd = etEditItem.getSelectionEnd();
        int selStart = etEditItem.getSelectionStart();

        gui.hideSoftKeyboard(etEditItem);
        numericKeyboard.setVisible(false);

        etEditItem.setInputType(InputType.TYPE_CLASS_TEXT);
        etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);

        etEditItem.setSelection(selStart, selEnd);
    }

    public void toggleTypingHour() {
        toggleTyping(1);
    }

    public void toggleTypingDate() {
        toggleTyping(2);
    }

    public void toggleTypingNumeric() {
        toggleTyping(3);
    }

    public void toggleTyping(int mode) {
        if (numericKeyboard.isVisible() && numericKeyboard.getTypingMode() == mode) {
            showAlphanumKeyboard();
        } else {
            showNumericKeyboard();
            numericKeyboard.startTyping(mode);
        }
    }

    @Override
    public void onNumKeyboardClosed() {
        showAlphanumKeyboard();
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {

    }

    public boolean editItemBackClicked() {
        if (numericKeyboard.isVisible()) {
            hideKeyboards();
            return true; //przechwycenie wciśnięcia przycisku
        }
        return false;
    }
}
