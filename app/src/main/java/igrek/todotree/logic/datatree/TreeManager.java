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

    public TreeManager() {
        this(new TreeItem(null, "root"));
    }

    public TreeManager(TreeItem rootItem) {
        this.rootItem = rootItem;
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
        Output.log("odczyt drzewka");

        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);

        if (!files.exists(dbFilePath.toString())) {

            Output.log("plik z bazą danych nie istnieje");
            return;
        }

        try {
            String fileContent = files.openFileString(dbFilePath.toString());
            TreeItem rootItem = TreeSerializer.loadTree(fileContent);
            setRootItem(rootItem);
        } catch (IOException | ParseException e) {
            Output.error(e);
        }

    }

    public void saveRootTree(Files files, Preferences preferences) {
        Output.log("zapis drzewka");

        PathBuilder dbFilePath = files.pathSD().append(preferences.dbFilePath);

        try {
            String output = TreeSerializer.saveTree(getRootItem());
            files.saveFile(dbFilePath.toString(), output);
        } catch (IOException e) {
            Output.error(e);
        }
    }
}
