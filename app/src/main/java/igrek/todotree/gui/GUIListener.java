package igrek.todotree.gui;

import android.view.View;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {
    void onToolbarBackClicked();

    void onAddItemClicked();

    void onItemClicked(int position, TreeItem item);

    void onItemEditClicked(int position, TreeItem item);

    void onItemRemoveClicked(int position, TreeItem item);

    void onItemMoveButtonPressed(int position, TreeItem item, View itemView);

    void onItemMoveButtonReleased(int position, TreeItem item, View itemView);

    void onSavedEditedItem(TreeItem editedItem, String content);

    void onSavedNewItem(String content);

    void onItemsSwapped(int pos1, int pos2);
}
