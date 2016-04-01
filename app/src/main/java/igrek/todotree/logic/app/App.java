package igrek.todotree.logic.app;

import android.support.v7.app.AppCompatActivity;

import java.util.List;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.GUIListener;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.system.output.Output;

//  WERSJA v1.03
//TODO: poprawienie layoutów po zmianie theme
//TODO: zrobić porządek w stylach: layout i values/styles

//TODO: zaznaczanie wielu elementów + usuwanie, kopiowanie, przenoszenie, wycinanie
//TODO: akcja long pressed do tree itemów - wybór większej ilości opcji: multiselect (zmiana przycisków akcji na inne funkcje)
//TODO: podczas edycji, przyciski przesuwania kursora (do początku, 1 w lewo, 1 w prawo, do końca), zaznacz wszystko
//TODO: pole edycyjne multiline przy overflow
//TODO: multiline tekstu itemu, przy overflowie (różny rozmiar itemów - poprawienie przewijania i przemieszczania)

//  NOWE FUNKCJONALNOŚCI
//TODO: kopiowanie i wklejanie elementów
//TODO: gesty do obsługi powrotu w górę, dodania nowego elementu, wejścia w element
//TODO: różne akcje na kliknięcie elementu: (wejście - folder, edycja - element), przycisk wejścia dla pojedynczych elementów
//TODO: wchodzenie do środka drzewa poprzez gesty (smyranie w prawo), w górę (smyranie w lewo)
//TODO: zapisywanie kilku ostatnich wersji bazy danych (backup)
//TODO: system logów z wieloma poziomami (info - jeden z poziomów, wyświetlany użytkownikowi)
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania
//TODO: moduł obliczeń: sumowanie elementów, inline calc
//TODO: klasy elementów: checkable (z pamięcią stanu), separator
//TODO: breadcrumbs przy nazwie aktualnego elementu
//TODO: tryb landscape screen przy pisaniu z klawiatury ekranowej
//TODO: powrót w górę scrolluje lista do rodzica
//TODO: zapisanie stałej konfiguracji w Config lub XML
//TODO: przywrócenie minSdkVersion 13

//TODO: KONFIGURACJA:
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza, tryb landscape screen przy pisaniu z klawiatury ekranowej

//  WYGLĄD
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
        }else if (id == R.id.action_minimize) {
            minimize();
            return true;
        }else if (id == R.id.action_exit_without_saving) {
            exitApp(false);
            return true;
        }else if (id == R.id.action_save_exit) {
            exitApp(true);
            return true;
        }else if (id == R.id.action_save) {
            treeManager.saveRootTree(files, preferences);
            showInfo("Zapisano bazę danych.");
            return true;
        }else if (id == R.id.action_reload) {
            treeManager = new TreeManager();
            treeManager.loadRootTree(files, preferences);
            updateItemsList();
            showInfo("Wczytano bazę danych.");
            return true;
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
            goUp();
        } else if (state == AppState.EDIT_ITEM_CONTENT) {
            gui.hideSoftKeyboard();
            discardEditingItem();
        }
    }

    private void exitApp(boolean withSaving){
        if(withSaving) {
            treeManager.saveRootTree(files, preferences);
        }
        quit();
    }

    private void updateItemsList(){
        gui.updateItemsList(treeManager.getCurrentItem(), treeManager.getSelectedItems());
        state = AppState.ITEMS_LIST;
    }


    @Override
    public void onToolbarBackClicked() {
        backClicked();
    }

    @Override
    public void onAddItemClicked() {
        newItem(-1);
    }

    @Override
    public void onAddItemClicked(int position) {
        newItem(position);
    }

    @Override
    public void onItemClicked(int position, TreeItem item) {
        treeManager.goInto(position);
        updateItemsList();
    }

    @Override
    public void onItemEditClicked(int position, TreeItem item) {
        editItem(item, treeManager.getCurrentItem());
    }

    @Override
    public void onItemRemoveClicked(int position, TreeItem item) {
        removeItem(position);
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
        Output.log("long click");
        if(!treeManager.isSelectionMode()){
            treeManager.startSelectionMode();
            treeManager.setItemSelected(position, true);
            Output.log("start selection mode");
        }else{
            treeManager.setItemSelected(position, true);
            Output.log("continue selection mode");
        }

        for(Integer itemPos : treeManager.getSelectedItems()){
            Output.log("selected: "+itemPos.intValue());
        }

        updateItemsList();


    }
}
