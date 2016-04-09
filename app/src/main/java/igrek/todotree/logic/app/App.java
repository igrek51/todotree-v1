package igrek.todotree.logic.app;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.GUIListener;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.system.output.Output;

//  WERSJA v1.03
//TODO: poprawienie przewijania i przemieszczania dla różnego rozmiaru itemów
//TODO: przewijanie do ostatniego itemu lub rodzica po powrocie (w górę, zapis, anulowanie edycji)
//TODO: rescroll po wejsciu do gałęzi
//TODO: rescroll przy wejściu w tryb zaznaczania (zmiana rozmiaru itemów)

//TODO: nieprzykrywanie przycisku plus przez info bar

//TODO: szybkie przewijanie na koniec i początek listy elementów

//  NOWE FUNKCJONALNOŚCI
//TODO: zmiana widoczności opcji menu przy zaznaczaniu wielu elementów i kopiowaniu (niepusty schowek, niepuste zaznaczenie)
//TODO: podczas edycji, przyciski przesuwania kursora (do początku, 1 w lewo, 1 w prawo, do końca), zaznacz wszystko
//TODO: gesty do obsługi powrotu w górę (smyranie w lewo), dodania nowego elementu, wejścia w element (smyranie w prawo)
//TODO: różne akcje na kliknięcie elementu: (wejście - folder, edycja - element), przycisk wejścia dla pojedynczych elementów
//TODO: zapisywanie kilku ostatnich wersji bazy danych (backup)
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania
//TODO: moduł obliczeń: inline calc
//TODO: moduł obliczeń: sumowanie wielu elementów
//TODO: wyjście bez zapisywania bazy jeśli nie było zmian
//TODO: przycisk zaznaczania wszystkich elementów
//TODO: klawiatura szybkiego wpisywania godzin (i dat)
//TODO: system logów z wieloma poziomami (info - jeden z poziomów, wyświetlany użytkownikowi)
//TODO: klasy elementów: checkable (z pamięcią stanu), separator
//TODO: breadcrumbs przy nazwie aktualnego elementu
//TODO: tryb landscape screen przy pisaniu z klawiatury ekranowej
//TODO: zapisanie stałej konfiguracji w Config lub XML

//TODO: KONFIGURACJA:
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza, tryb landscape screen przy pisaniu z klawiatury ekranowej

//  WYGLĄD
//TODO: przycisk przesuwania itemów - maksymalna wysokość = wysokość itemu
//TODO: liczebność elementów folderu jako osobny textedit z szarym kolorem i wyrównany do prawej, w tytule rodzica to samo
//TODO: motyw kolorystyczny, pasek stanu, zapisanie wszystkich kolorów w xml, metoda do wyciągania kolorów z zasobów
//TODO: konfiguracja: wyświetlacz zawsze zapalony, wielkość czcionki, marginesy między elementami
//TODO: ikona aplikacji

public class App extends BaseApp implements GUIListener {

    TreeManager treeManager;
    GUI gui;

    AppState state;

    public App(AppCompatActivity activity) {
        super(activity);

        preferences.preferencesLoad();

        treeManager = new TreeManager();
        treeManager.loadRootTree(files, preferences);

        gui = new GUI(activity, this);
        gui.setTouchController(this);
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;

        Output.log("Aplikacja uruchomiona.");
    }

    @Override
    public void quit() {
        preferences.preferencesSave();
        super.quit();
    }

    @Override
    public boolean optionsSelect(int id) {
        if (id == R.id.action_settings) {

            return true;
        } else if (id == R.id.action_minimize) {
            minimize();
            return true;
        } else if (id == R.id.action_exit_without_saving) {
            exitApp(false);
            return true;
        } else if (id == R.id.action_save_exit) {
            exitApp(true);
            return true;
        } else if (id == R.id.action_save) {
            treeManager.saveRootTree(files, preferences);
            showInfo("Zapisano bazę danych.");
            return true;
        } else if (id == R.id.action_reload) {
            treeManager = new TreeManager();
            treeManager.loadRootTree(files, preferences);
            updateItemsList();
            showInfo("Wczytano bazę danych.");
            return true;
        } else if (id == R.id.action_copy) {
            copySelectedItems(true);
        } else if (id == R.id.action_cut) {
            cutSelectedItems();
        } else if (id == R.id.action_paste) {
            pasteItems();
        }
        return false;
    }

    @Override
    public boolean onKeyBack() {
        backClicked();
        return true;
    }


    public void showInfo(String info) {
        showInfo(info, gui.getMainContent());
    }


    @Override
    public void menuInit(Menu menu) {
        super.menuInit(menu);
        //setMenuItemVisible(R.id.action_copy, false);
        //        item.setTitle(title);
        //        item.setIcon(iconRes); //int iconRes
    }


    /**
     * @param position pozycja nowego elementu (0 - początek, ujemna wartość - na końcu listy)
     */
    public void newItem(int position) {
        if (position < 0) position = treeManager.getCurrentItem().size();
        if (position > treeManager.getCurrentItem().size())
            position = treeManager.getCurrentItem().size();
        treeManager.setNewItemPosition(position);
        gui.showEditItemPanel(null, treeManager.getCurrentItem());
        state = AppState.EDIT_ITEM_CONTENT;
    }

    private void editItem(TreeItem item, TreeItem parent) {
        treeManager.setEditItem(item);
        gui.showEditItemPanel(item, parent);
        state = AppState.EDIT_ITEM_CONTENT;
    }

    private void discardEditingItem() {
        treeManager.setEditItem(null);
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Anulowano edycję elementu.");
    }

    private void removeItem(int position) {
        treeManager.getCurrentItem().remove(position);
        updateItemsList();
        showInfo("Usunięto element.");
    }

    private void removeSelectedItems(boolean info) {
        List<Integer> selectedIds = treeManager.getSelectedItems();
        //posortowanie malejąco (żeby przy usuwaniu nie nadpisać indeksów)
        Collections.sort(selectedIds, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs.compareTo(lhs);
            }
        });
        for(Integer id : selectedIds){
            treeManager.getCurrentItem().remove(id);
        }
        if(info) {
            showInfo("Usunięto zaznaczone elementy: " + selectedIds.size());
        }
        treeManager.cancelSelectionMode();
        updateItemsList();
    }

    public void goUp() {
        try {
            treeManager.goUp();
            updateItemsList();
        } catch (NoSuperItemException e) {
            exitApp(true);
        }
    }

    private void backClicked() {
        if (state == AppState.ITEMS_LIST) {
            if (treeManager.isSelectionMode()) {
                treeManager.cancelSelectionMode();
            } else {
                goUp();
            }
            updateItemsList();
        } else if (state == AppState.EDIT_ITEM_CONTENT) {
            gui.hideSoftKeyboard();
            discardEditingItem();
        }
    }

    private void exitApp(boolean withSaving) {
        if (withSaving) {
            treeManager.saveRootTree(files, preferences);
        }
        quit();
    }

    private void updateItemsList() {
        gui.updateItemsList(treeManager.getCurrentItem(), treeManager.getSelectedItems());
        state = AppState.ITEMS_LIST;
    }

    private void copySelectedItems(boolean info) {
        if (treeManager.isSelectionMode()) {
            treeManager.clearClipboard();
            for(Integer selectedItemId : treeManager.getSelectedItems()) {
                TreeItem selectedItem = treeManager.getCurrentItem().getChild(selectedItemId);
                treeManager.addToClipboard(selectedItem);
            }
            if(info){
                showInfo("Skopiowano zaznaczone elementy: " + treeManager.getSelectedItemsCount());
            }
        }
    }

    private void cutSelectedItems() {
        copySelectedItems(false);
        showInfo("Wycięto zaznaczone elementy: " + treeManager.getSelectedItemsCount());
        removeSelectedItems(false);
    }

    private void pasteItems(){
        if(treeManager.isClipboardEmpty()){
            showInfo("Schowek jest pusty.");
        }else{
            for(TreeItem clipboardItem : treeManager.getClipboard()){
                clipboardItem.setParent(treeManager.getCurrentItem());
                treeManager.addToCurrent(clipboardItem);
            }
            showInfo("Wklejono elementy: " + treeManager.getClipboardSize());
            treeManager.recopyClipboard();
            updateItemsList();
        }

    }


    @Override
    public void onToolbarBackClicked() {
        backClicked();
    }

    @Override
    public void onAddItemClicked() {
        onAddItemClicked(-1);
    }

    @Override
    public void onAddItemClicked(int position) {
        treeManager.cancelSelectionMode();
        newItem(position);
    }

    @Override
    public void onItemClicked(int position, TreeItem item) {
        if(treeManager.isSelectionMode()) {
            treeManager.toggleItemSelected(position);
        }else{
            treeManager.goInto(position);
        }
        updateItemsList();
    }

    @Override
    public void onItemEditClicked(int position, TreeItem item) {
        treeManager.cancelSelectionMode();
        editItem(item, treeManager.getCurrentItem());
    }

    @Override
    public void onItemRemoveClicked(int position, TreeItem item) {
        if (treeManager.isSelectionMode()) {
            removeSelectedItems(true);
        } else {
            removeItem(position);
        }
    }

    @Override
    public void onSavedEditedItem(TreeItem editedItem, String content) {
        content = treeManager.trimContent(content);
        if (content.isEmpty()) {
            treeManager.getCurrentItem().remove(editedItem);
            showInfo("Pusty element został usunięty.");
        } else {
            editedItem.setContent(content);
            showInfo("Zapisano element.");
        }
        state = AppState.ITEMS_LIST;
        gui.showItemsList(treeManager.getCurrentItem());
    }

    @Override
    public void onSavedNewItem(String content) {
        content = treeManager.trimContent(content);
        if (content.isEmpty()) {
            showInfo("Pusty element został usunięty.");
        } else {
            treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
            showInfo("Zapisano nowy element.");
        }
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
    }

    @Override
    public List<TreeItem> onItemMoved(int position, int step) {
        treeManager.move(treeManager.getCurrentItem(), position, step);
        return treeManager.getCurrentItem().getChildren();
    }

    @Override
    public void onCancelEditedItem(TreeItem editedItem) {
        gui.hideSoftKeyboard();
        discardEditingItem();
    }

    @Override
    public void onSelectedClicked(int position, TreeItem item, boolean checked) {
        treeManager.setItemSelected(position, checked);
        updateItemsList();
    }

    @Override
    public void onItemLongClick(int position) {
        if (!treeManager.isSelectionMode()) {
            treeManager.startSelectionMode();
            treeManager.setItemSelected(position, true);
        } else {
            treeManager.setItemSelected(position, true);
        }
        updateItemsList();
    }
}
