package igrek.todotree.gui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import igrek.todotree.R;
import igrek.todotree.gui.treelist.TreeItemAdapter;
import igrek.todotree.gui.treelist.TreeItemListener;
import igrek.todotree.logic.datatree.TreeItem;

public class GUI implements TreeItemListener {
    AppCompatActivity activity;
    GUIListener GUIListener;

    RelativeLayout mainContent;

    TextView listTitle;
    TreeItemAdapter itemsListAdapter;

    public GUI(AppCompatActivity activity, GUIListener GUIListener) {
        this.activity = activity;
        this.GUIListener = GUIListener;
        init();
    }

    private void init() {
        activity.setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GUIListener.onToolbarBackClick();
            }
        });

        //  główna zawartość
        mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
    }

    public RelativeLayout getMainContent() {
        return mainContent;
    }

    private View setMainContentLayout(int layoutResource) {
        mainContent.removeAllViews();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutResource, null);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainContent.addView(layout);
        return layout;
    }

    public void showItemsList(TreeItem currentItem) {
        View itemsListLayout = setMainContentLayout(R.layout.items_list);

        //przycisk dodawania
        FloatingActionButton fab = (FloatingActionButton) itemsListLayout.findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(0xff0080a0));
        fab.setColorFilter(0xff000060);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GUIListener.onAddItemButtonClicked();
            }
        });

        listTitle = (TextView) itemsListLayout.findViewById(R.id.list_title);

        ListView itemsListView = (ListView) itemsListLayout.findViewById(R.id.listview1);

        itemsListAdapter = new TreeItemAdapter(activity, currentItem.getChildren(), this);
        itemsListView.setAdapter(itemsListAdapter);

        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                TreeItem item = (TreeItem) parent.getItemAtPosition(position);
                GUIListener.onTreeItemClicked(position, item);
            }
        });

        updateItemsList(currentItem);
    }

    public void showEditItemPanel(final TreeItem item, TreeItem parent) {
        View editItemContentLayout = setMainContentLayout(R.layout.edit_item_content);

        TextView tv_item_title = (TextView) editItemContentLayout.findViewById(R.id.tv_item_title);
        final EditText et_edit_item = (EditText) editItemContentLayout.findViewById(R.id.et_edit_item);
        Button button_save_item = (Button) editItemContentLayout.findViewById(R.id.button_save_item);

        tv_item_title.setText(getTreeItemText(parent));

        et_edit_item.setText(item.getContent());

        button_save_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GUIListener.onSavedEditedItem(item, et_edit_item.getText().toString());
                hideSoftKeyboard(et_edit_item);
            }
        });
    }

    private String getTreeItemText(TreeItem item) {
        StringBuilder sb = new StringBuilder(item.getContent());
        sb.append(" [");
        sb.append(item.size());
        sb.append("]:");
        return sb.toString();
    }

    public void updateItemsList(TreeItem currentItem) {
        //tytuł gałęzi
        listTitle.setText(getTreeItemText(currentItem));
        //lista elementów
        itemsListAdapter.setDataSource(currentItem.getChildren());
    }

    private void hideSoftKeyboard(View window) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
    }

    @Override
    public void onTreeItemEditClicked(int position, TreeItem item) {
        GUIListener.onTreeItemEditClicked(position, item);
    }

    @Override
    public void onTreeItemRemoveClicked(int position, TreeItem item) {
        GUIListener.onTreeItemRemoveClicked(position, item);
    }
}
