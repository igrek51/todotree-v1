package igrek.todotree.gui;

import java.util.List;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {
    void onToolbarBackClicked();

    void onAddItemClicked();

    void onItemClicked(int position, TreeItem item);

    void onItemEditClicked(int position, TreeItem item);

    void onItemRemoveClicked(int position, TreeItem item);

    void onSavedEditedItem(TreeItem editedItem, String content);

    void onSavedNewItem(String content);

    /**
     * @param position pozycja elementu przed przesuwaniem
     * @param step liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     * @return nowa lista elementów
     */
    List<TreeItem> onItemMoved(int position, int step);
}
