package igrek.todotree.logic.datatree;

import java.io.IOException;
import java.text.ParseException;

import igrek.todotree.logic.datatree.serializer.TreeSerializer;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.settings.preferences.Preferences;
import igrek.todotree.system.files.Files;
import igrek.todotree.system.files.PathBuilder;
import igrek.todotree.system.output.Output;

public class TreeManager {
    private TreeItem rootItem;
    private TreeItem currentItem;

    private TreeItem editItem = null;
    private Integer newItemPosition = null;

    private TreeSerializer treeSerializer = new TreeSerializer();

    public TreeManager() {
        rootItem = new TreeItem(null, "root");
        currentItem = rootItem;
        editItem = null;
    }

    public TreeItem getRootItem() {
        return rootItem;
    }

    public void setRootItem(TreeItem rootItem) {
        this.rootItem = rootItem;
        this.currentItem = rootItem;
    }

    public TreeItem getCurrentItem() {
        return currentItem;
    }

    public TreeItem getEditItem() {
        return editItem;
    }

    public void setEditItem(TreeItem editItem) {
        this.editItem = editItem;
        this.newItemPosition = null;
    }

    public void setNewItemPosition(Integer newItemPosition) {
        this.editItem = null;
        this.newItemPosition = newItemPosition;
    }

    public Integer getNewItemPosition() {
        return newItemPosition;
    }

    //  NAWIGACJA

    public void goUp() throws NoSuperItemException {
        if (currentItem == rootItem) {
            throw new NoSuperItemException();
        } else if (currentItem.getParent() == null) {
            throw new IllegalStateException("parent = null. To się nie powinno zdarzyć");
        } else {
            currentItem = currentItem.getParent();
        }
    }

    public void goInto(int childIndex) {
        goTo(currentItem.getChild(childIndex));
    }

    public void goTo(TreeItem child) {
        currentItem = child;
    }

    public void goToRoot() {
        goTo(rootItem);
    }

    //  DODAWANIE / USUWANIE ELEMENTÓW

    public void addToCurrent(TreeItem newItem) {
        currentItem.add(newItem);
    }

    public void deleteFromCurrent(int location) {
        currentItem.remove(location);
    }

    //  EDYCJA

    public void saveItemContent(TreeItem item, String content) {
        item.setContent(content);
    }

    public void saveItemContent(String content) {
        if (editItem != null) {
            editItem.setContent(content);
        }
    }

    //  ZAPIS / ODCZYT Z PLIKU

    public void loadRootTree(Files files, Preferences preferences) {
        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);
        Output.log("Wczytywanie bazy danych z pliku: " + dbFilePath.toString());
        if (!files.exists(dbFilePath.toString())) {
            Output.log("Plik z bazą danych nie istnieje. Domyślna pusta baza danych.");
            return;
        }
        try {
            String fileContent = files.openFileString(dbFilePath.toString());
            TreeItem rootItem = treeSerializer.loadTree(fileContent);
            setRootItem(rootItem);
            Output.log("Wczytano bazę danych.");
        } catch (IOException | ParseException e) {
            Output.error(e);
        }
    }

    public void saveRootTree(Files files, Preferences preferences) {
        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);
        Output.log("Zapisywanie bazy danych do pliku: " + dbFilePath.toString());
        try {
            String output = treeSerializer.saveTree(getRootItem());
            files.saveFile(dbFilePath.toString(), output);
            Output.log("Zapisano bazę danych.");
        } catch (IOException e) {
            Output.error(e);
        }
    }

    //  ZMIANA KOLEJNOŚCI

    public void replace(TreeItem parent, int pos1, int pos2) {
        if (pos1 == pos2) return;
        if (pos1 < 0 || pos2 < 0) throw new IllegalArgumentException("position < 0");
        if (pos1 >= parent.size() || pos2 >= parent.size())
            throw new IllegalArgumentException("position >= size");
        TreeItem item1 = parent.getChild(pos1);
        TreeItem item2 = parent.getChild(pos2);
        //wstawienie na pos1
        parent.remove(pos1);
        parent.add(pos1, item2);
        //wstawienie na pos2
        parent.remove(pos2);
        parent.add(pos2, item1);
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w górę
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    public void moveUp(TreeItem parent, int position) {
        if (position <= 0) return;
        replace(parent, position, position - 1);
    }

    /**
     * przesuwa element z pozycji o jedną pozycję w dół
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     */
    public void moveDown(TreeItem parent, int position) {
        if (position >= parent.size() - 1) return;
        replace(parent, position, position + 1);
    }

    /**
     * przesuwa element z pozycji o określoną liczbę pozycji
     *
     * @param parent   przodek przesuwanego elementu
     * @param position pozycja elementu przed przesuwaniem
     * @param step     liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     */
    public void move(TreeItem parent, int position, int step) {
        int targetPosition = position + step;
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= parent.size()) targetPosition = parent.size() - 1;
        while (position < targetPosition) {
            moveDown(parent, position);
            position++;
        }
        while (position > targetPosition) {
            moveUp(parent, position);
            position--;
        }
    }

    /**
     * przesuwa element z pozycji do wybranego miejsca
     *
     * @param parent         przodek przesuwanego elementu
     * @param position       pozycja elementu przed przesuwaniem
     * @param targetPosition pozycja elemetnu po przesuwaniu
     */
    public void moveTo(TreeItem parent, int position, int targetPosition) {
        if (targetPosition < 0) targetPosition = 0;
        if (targetPosition >= parent.size()) targetPosition = parent.size() - 1;
        while (position < targetPosition) {
            moveDown(parent, position);
            position++;
        }
        while (position > targetPosition) {
            moveUp(parent, position);
            position--;
        }
    }

    public void moveToEnd(TreeItem parent, int position) {
        moveTo(parent, position, parent.size() - 1);
    }

    public void moveToBegining(TreeItem parent, int position) {
        moveTo(parent, position, 0);
    }
}
