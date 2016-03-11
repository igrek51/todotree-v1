package igrek.todotree.gui;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {
    void onToolbarBackClick();

    void onAddItemButtonClicked();

    void onTreeItemClicked(int position, TreeItem item);

    void onTreeItemEditClicked(int position, TreeItem item);

    void onTreeItemRemoveClicked(int position, TreeItem item);

    void onSavedEditedItem(TreeItem editedItem, String content);
}
