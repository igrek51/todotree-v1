package igrek.todotree.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import igrek.todotree.R;
import igrek.todotree.gui.treelist.TreeItemAdapter;
import igrek.todotree.logic.datatree.TreeItem;

public class GUI extends GUIBase {

    TextView listTitle;
    TreeItemAdapter itemsListAdapter;
    EditText etEditItem;

    public GUI(AppCompatActivity activity, GUIListener guiListener) {
        super(activity, guiListener);
    }

    @Override
    protected void init() {
        activity.setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setDisplayShowHomeEnabled(true);
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

        listTitle = (TextView) itemsListLayout.findViewById(R.id.list_title);

        ListView itemsListView = (ListView) itemsListLayout.findViewById(R.id.listview1);

        itemsListAdapter = new TreeItemAdapter(activity, currentItem.getChildren(), guiListener);
        itemsListView.setAdapter(itemsListAdapter);

        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                if (position == itemsListAdapter.getCount() - 1) {
                    //nowy element
                    guiListener.onAddItemClicked();
                } else {
                    //istniejący element
                    TreeItem item = itemsListAdapter.getItem(position);
                    guiListener.onItemClicked(position, item);
                }
            }
        });

        updateItemsList(currentItem);
    }

    public void showEditItemPanel(final TreeItem item, TreeItem parent) {
        View editItemContentLayout = setMainContentLayout(R.layout.edit_item_content);

        TextView tvItemTitle = (TextView) editItemContentLayout.findViewById(R.id.tv_item_title);
        etEditItem = (EditText) editItemContentLayout.findViewById(R.id.et_edit_item);
        Button buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.button_save_item);

        tvItemTitle.setText(parent.getContent() + ":");

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

        //focus na końcu edytowanego tekstu
        etEditItem.requestFocus();
        etEditItem.setSelection(etEditItem.getText().length());
        showSoftKeyboard(etEditItem);
    }

    public void updateItemsList(TreeItem currentItem) {
        //tytuł gałęzi
        listTitle.setText(getTreeItemText(currentItem));
        //lista elementów
        itemsListAdapter.setDataSource(currentItem.getChildren());
    }

    public void hideSoftKeyboard() {
        hideSoftKeyboard(etEditItem);
    }
}
