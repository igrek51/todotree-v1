package igrek.todotree.gui;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {
    void onToolbarBackClicked();

    void onAddItemClicked();

    void onItemClicked(int position, TreeItem item);

    void onItemEditClicked(int position, TreeItem item);

    void onItemRemoveClicked(int position, TreeItem item);

    void onSavedEditedItem(TreeItem editedItem, String content);

    void onSavedNewItem(String content);
}
