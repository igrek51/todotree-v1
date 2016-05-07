package igrek.todotree.gui.views.edititem;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.logic.datatree.TreeItem;

public class EditItemGUI {

    private GUI gui;
    private EditText etEditItem;

    public EditItemGUI(GUI gui, final TreeItem item, TreeItem parent) {
        this.gui = gui;
        init(item, parent);
    }

    public EditText getEtEditItem() {
        return etEditItem;
    }

    public void init(final TreeItem item, TreeItem parent){
        View editItemContentLayout = gui.setMainContentLayout(R.layout.edit_item_content);

        etEditItem = (EditText) editItemContentLayout.findViewById(R.id.etEditItemContent);
        //przycisk zapisu
        Button buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.buttonSaveItem);
        //przycisk zapisz i dodaj nowy
        Button buttonSaveAndAdd = (Button) editItemContentLayout.findViewById(R.id.buttonSaveAndAddItem);

        gui.setTitle(parent.getContent());

        if (item != null) { //edycja
            etEditItem.setText(item.getContent());
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSavedEditedItem(item, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(item, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
        } else { //nowy element
            etEditItem.setText("");
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSavedNewItem(etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
            buttonSaveAndAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gui.getGuiListener().onSaveAndAddItem(null, etEditItem.getText().toString());
                    gui.hideSoftKeyboard(etEditItem);
                }
            });
        }

        //przycisk anuluj
        Button buttonEditCancel = (Button) editItemContentLayout.findViewById(R.id.buttonEditCancel);
        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gui.getGuiListener().onCancelEditedItem(item);
            }
        });

        //przyciski zmiany kursora i zaznaczenia
        ImageButton quickEditGoBegin = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoBegin);
        quickEditGoBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(-2);
            }
        });
        ImageButton quickEditGoLeft = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoLeft);
        quickEditGoLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(-1);
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
                quickEdit(+1);
            }
        });
        ImageButton quickEditGoEnd = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoEnd);
        quickEditGoEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickEdit(+2);
            }
        });

        //focus na końcu edytowanego tekstu
        etEditItem.requestFocus();
        quickEdit(+2);
        gui.showSoftKeyboard(etEditItem);
    }

    private void quickEdit(int direction){
        if(direction == -2){ //na początek
            etEditItem.setSelection(0);
        }else if(direction == +2){ //na koniec
            etEditItem.setSelection(etEditItem.getText().length());
        }else if(direction == -1 || direction == +1){
            int selStart = etEditItem.getSelectionStart();
            int selEnd = etEditItem.getSelectionEnd();
            if(selStart == selEnd){ //brak zaznaczenia
                selStart += direction;
                if(selStart < 0) selStart = 0;
                if(selStart > etEditItem.getText().length()) selStart = etEditItem.getText().length();
                etEditItem.setSelection(selStart);
            }else{ //zaznaczenie wielu znaków
                //poszerzenie zaznaczenia
                if(direction == -1){ //w lewo
                    selStart--;
                    if(selStart < 0) selStart = 0;
                }else if(direction == +1){ //w prawo
                    selEnd++;
                    if(selEnd > etEditItem.getText().length()) selEnd = etEditItem.getText().length();
                }
                etEditItem.setSelection(selStart, selEnd);
            }
        }
    }

    private void quickEditSelectAll(){
        etEditItem.setSelection(0, etEditItem.getText().length());
    }
}
