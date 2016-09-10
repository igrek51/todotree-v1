package igrek.todotree.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.List;

import igrek.todotree.R;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.logic.controller.AppController;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.events.ToolbarBackClickedEvent;

//  WYGLĄD
//TODO: liczebność elementów folderu jako osobny textedit z szarym kolorem i wyrównany do prawej, w tytule rodzica to samo
//TODO: zapisanie wszystkich kolorów w xml, metoda do wyciągania kolorów z zasobów
//TODO: zmniejszyć padding przycisków w navbarze

public class GUI extends GUIBase {

    private EditText etEditItem;
    private ActionBar actionBar;
    private TreeListView itemsListView;
    private EditItemGUI editItemGUI = null;

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
                AppController.sendEvent(new ToolbarBackClickedEvent());
            }
        });

        //  główna zawartość
        mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
    }

    public void showItemsList(final TreeItem currentItem) {
        View itemsListLayout = setMainContentLayout(R.layout.items_list);

        itemsListView = (TreeListView) itemsListLayout.findViewById(R.id.treeItemsList);

        itemsListView.init(activity, guiListener);

        itemsListView.setItems(currentItem.getChildren());

        //itemsListView.setOnTouchListener(this);

        updateItemsList(currentItem, null);
    }

    public void showEditItemPanel(final TreeItem item, TreeItem parent) {
        editItemGUI = new EditItemGUI(this, item, parent);
        etEditItem = editItemGUI.getEtEditItem();
    }

    public void showExitScreen(){
        View exitScreen = setMainContentLayout(R.layout.exit_screen);
    }

    public void updateItemsList(TreeItem currentItem, List<Integer> selectedPositions) {
        //tytuł gałęzi
        StringBuilder sb = new StringBuilder(currentItem.getContent());
        if(!currentItem.isEmpty()) {
            sb.append(" [");
            sb.append(currentItem.size());
            sb.append("]");
        }
        setTitle(sb.toString());
        //lista elementów
        itemsListView.setItemsAndSelected(currentItem.getChildren(), selectedPositions);
    }

    public void scrollToItem(int position){
        itemsListView.scrollToItem(position);
    }

    public void scrollToPosition(int y) {
        itemsListView.scrollToPosition(y);
    }

    public void scrollToBottom() {
        itemsListView.scrollToBottom();
    }

    public void hideSoftKeyboard() {
        editItemGUI.hideKeyboards();
    }

    public boolean editItemBackClicked() {
        return editItemGUI.editItemBackClicked();
    }

    public void setTitle(String title){
        //TODO: breadcrumbs przy nazwie aktualnego elementu
        actionBar.setTitle(title);
    }

    public Integer getCurrentScrollPos() {
        return itemsListView.getCurrentScrollPosition();
    }

    public void requestSaveEditedItem() {
        editItemGUI.requestSaveEditedItem();
    }
}
