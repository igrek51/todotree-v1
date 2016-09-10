package igrek.todotree.gui;

import java.util.List;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {

    /**
     * @param position pozycja elementu przed przesuwaniem
     * @param step liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     * @return nowa lista elementów
     */
    List<TreeItem> onItemMoved(int position, int step);
}
