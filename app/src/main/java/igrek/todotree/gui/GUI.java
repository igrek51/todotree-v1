package igrek.todotree.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import igrek.todotree.R;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.logic.datatree.TreeItem;

public class GUI extends GUIBase {

    EditText etEditItem;
    ActionBar actionBar;
    TreeListView itemsListView;

    public GUI(AppCompatActivity activity, GUIListener guiListener) {
        super(activity, guiListener);
    }

    @Override
    protected void init() {
        activity.setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);
        actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guiListener.onToolbarBackClicked();
            }
        });

        //  główna zawartość
        mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
    }

    public void showItemsList(final TreeItem currentItem) {
        View itemsListLayout = setMainContentLayout(R.layout.items_list);

        itemsListView = (TreeListView) itemsListLayout.findViewById(R.id.treelistview1);

        itemsListView.init(activity, guiListener);

        itemsListView.setItems(currentItem.getChildren());

        //itemsListView.setOnTouchListener(this);

        updateItemsList(currentItem);
    }

    public void showEditItemPanel(final TreeItem item, TreeItem parent) {
        View editItemContentLayout = setMainContentLayout(R.layout.edit_item_content);

        etEditItem = (EditText) editItemContentLayout.findViewById(R.id.et_edit_item);
        Button buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.button_save_item);
        Button buttonEditCancel = (Button) editItemContentLayout.findViewById(R.id.buttonEditCancel);

        setTitle(parent.getContent());

        if (item != null) { //edycja
            etEditItem.setText(item.getContent());
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guiListener.onSavedEditedItem(item, etEditItem.getText().toString());
                    hideSoftKeyboard(etEditItem);
                }
            });
        } else { //nowy element
            etEditItem.setText("");
            buttonSaveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guiListener.onSavedNewItem(etEditItem.getText().toString());
                    hideSoftKeyboard(etEditItem);
                }
            });
        }

        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guiListener.onCancelEditedItem(item);
            }
        });

        //focus na końcu edytowanego tekstu
        etEditItem.requestFocus();
        etEditItem.setSelection(etEditItem.getText().length());
        showSoftKeyboard(etEditItem);
    }

    public void updateItemsList(TreeItem currentItem) {
        //tytuł gałęzi
        StringBuilder sb = new StringBuilder(currentItem.getContent());
        if(!currentItem.isEmpty()) {
            sb.append(" [");
            sb.append(currentItem.size());
            sb.append("]");
        }
        setTitle(sb.toString());
        //lista elementów
        itemsListView.setItems(currentItem.getChildren());
    }

    public void hideSoftKeyboard() {
        hideSoftKeyboard(etEditItem);
    }

    public void setTitle(String title){
        actionBar.setTitle(title);
    }
}
