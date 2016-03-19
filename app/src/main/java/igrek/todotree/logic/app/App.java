package igrek.todotree.logic.app;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import igrek.todotree.R;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.GUIListener;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.system.output.Output;

//  WERSJA v1.1
//TODO: przesuwanie elementów, przesuwanie na koniec, na początek listy
//TODO: wychwycenie eventu resize'u okna (zmiany orientacji), brak restartu aplikacji, obsłużenie, odświeżenie layoutów

//  NOWE FUNKCJONALNOŚCI
//TODO: zabronienie używania znaków "{" i "}" w tekście (usuwanie ich)
//TODO: kopiowanie i wklejanie elementów
//TODO: utworzenie nowego elementu przed wybranym
//TODO: podczas edycji, przyciski przesuwania kursora (do początku, 1 w lewo, 1 w prawo, do końca), zaznacz wszystko
//TODO: minimalizacja aplikacji i wyjście z aplikacji (z zapisem i bez zapisu bazy), szybkie wyjście
//TODO: różne akcje na kliknięcie elementu: (wejście - folder, edycja - element), przycisk wejścia dla pojedynczych elementów
//TODO: akcja long pressed do tree itemów - wybór większej ilości opcji: multiselect, utworzenie nowego przed
//TODO: zapisywanie kilku ostatnich wersji bazy danych (backup)
//TODO: zaznaczanie wielu elementów + usuwanie, kopiowanie, wycinanie
//TODO: system logów z wieloma poziomami (info - jeden z poziomów, wyświetlany użytkownikowi)
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania
//TODO: moduł obliczeń: sumowanie elementów, inline calc
//TODO: gesty do obsługi powrotu w górę, dodania nowego elementu, wejścia w element
//TODO: klasy elementów: checkable (z pamięcią stanu), separator
//TODO: breadcrumbs przy nazwie aktualnego elementu
//TODO: tryb landscape screen przy pisaniu z klawiatury ekranowej

//TODO: KONFIGURACJA:
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza, tryb landscape screen przy pisaniu z klawiatury ekranowej

//  WYGLĄD
//TODO: padding dla przycisków akcji (zwiększenie aktywnego pola)
//TODO: motyw kolorystyczny, zapisanie wszystkich kolorów w Config lub w xml
//TODO: ustalenie marginesów w layoutach i wypozycjonowanie elementów
//TODO: konfiguracja: wyświetlacz zawsze zapalony, wielkość czcionki, marginesy między elementami
//TODO: pole edycyjne multiline przy overflow
//TODO: zmiana wyglądu obrazków z drawable (przycisk +)
//TODO: ikona aplikacji

public class App extends BaseApp implements GUIListener {

    TreeManager treeManager;
    GUI gui;

    AppState state;

    Integer movePosition = null;
    View moveView = null;

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
        treeManager.saveRootTree(files, preferences);
        preferences.preferencesSave();
        super.quit();
    }

    @Override
    public boolean optionsSelect(int id) {
        if (id == R.id.action_settings) {

            return true;
        }
        return false;
    }

    @Override
    public boolean keycodeBack() {
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
        if(position < 0) position = treeManager.getCurrentItem().size();
        if(position > treeManager.getCurrentItem().size()) position = treeManager.getCurrentItem().size();
        treeManager.setNewItemPosition(position);
        gui.showEditItemPanel(null, treeManager.getCurrentItem());
        state = AppState.EDIT_ITEM_CONTENT;
    }

    private void editItem(TreeItem item, TreeItem parent) {
        treeManager.setEditItem(item);
        gui.showEditItemPanel(item, parent);
        state = AppState.EDIT_ITEM_CONTENT;
    }

    private void discardEditingItem(){
        treeManager.setEditItem(null);
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Anulowano edycję elementu.");
    }

    public void saveItem(TreeItem editItem, String content) {
        editItem.setContent(content);
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Zapisano element.");
    }

    private void removeItem(int position) {
        treeManager.getCurrentItem().remove(position);
        gui.updateItemsList(treeManager.getCurrentItem());
        showInfo("Usunięto element.");
    }

    public void goUp() {
        try {
            treeManager.goUp();
            gui.updateItemsList(treeManager.getCurrentItem());
        } catch (NoSuperItemException e) {
            quit();
        }
    }

    private void backClicked(){
        if(state == AppState.ITEMS_LIST) {
            goUp();
        }else if(state == AppState.EDIT_ITEM_CONTENT) {
            gui.hideSoftKeyboard();
            discardEditingItem();
        }
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
    public void onItemClicked(int position, TreeItem item) {
        treeManager.goInto(position);
        gui.updateItemsList(treeManager.getCurrentItem());
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
        saveItem(editedItem, content);
    }

    @Override
    public void onSavedNewItem(String content) {
        treeManager.getCurrentItem().add(treeManager.getNewItemPosition(), content);
        gui.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Zapisano nowy element.");
    }


    @Override
    public void onItemsSwapped(int pos1, int pos2) {
        treeManager.replace(treeManager.getCurrentItem(), pos1, pos2);
        gui.updateItemsList(treeManager.getCurrentItem());
    }

    @Override
    public int onItemMoved(int position, int step) {
        int newPos = treeManager.move(treeManager.getCurrentItem(), position, step);
        gui.updateItemsList(treeManager.getCurrentItem());
        return newPos;
    }
}
