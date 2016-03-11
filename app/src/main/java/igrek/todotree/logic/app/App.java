package igrek.todotree.logic.app;

import android.support.v7.app.AppCompatActivity;

import igrek.todotree.R;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.logic.exceptions.NoSuperItemException;
import igrek.todotree.system.output.Output;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.GUIListener;

//  WERSJA v1.1
//TODO: anulowanie dodawania nowego elementu
//TODO: uporządkowanie logów
//TODO: anulowanie edycji przyciskiem powrotu i strzałką powrotu
//TODO: przycisk dodawania nowego elementu przesunięty pod koniec listy, tak żeby nie zasłaniał przycisków elementów
//TODO: przesuwanie elementów, przesuwanie na koniec, na początek listy

//  NOWA FUNKCJONALNOŚĆ
//TODO: zabronienie używania znaków "{" i "}" w tekście (usuwanie ich)
//TODO: kopiowanie i wklejanie elementów
//TODO: utworzenie nowego elementu przed wybranym
//TODO: podczas edycji, przyciski przesuwania kursora (do początku, 1 w lewo, 1 w prawo, do końca), zaznacz wszystko
//TODO: minimalizacja aplikacji i wyjście z aplikacji (z zapisem i bez zapisu bazy), szybkie wyjście
//TODO: wychwycenie eventu resize'u okna (zmiany orientacji), brak restartu aplikacji, obsłużenie, odświeżenie layoutów
//TODO: różne akcje na kliknięcie elementu: (wejście - folder, edycja - element), przycisk wejścia dla pojedynczych elementów
//TODO: akcja long pressed do tree itemów - wybór większej ilości opcji: multiselect, utworzenie nowego przed
//TODO: zapisywanie kilku ostatnich wersji bazy danych (backup)
//TODO: zaznaczanie wielu elementów + usuwanie, kopiowanie, wycinanie
//TODO: system logów z wieloma poziomami (info - jeden z poziomów, wyświetlany użytkownikowi)
//TODO: funkcja cofania zmian - zapisywanie modyfikacji, dodawania, usuwania elementów, przesuwania
//TODO: moduł obliczeń: sumowanie elementów, inline calc
//TODO: gesty do obsługi powrotu w górę, dodania nowego elementu, wejścia w element
//TODO: klasy elementów: checkable (z pamięcią stanu), separator

//TODO: KONFIGURACJA:
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza

//  WYGLĄD
//TODO: edycja: domyślnie kursor na końcu tekstu, focus na start, pokazanie kalwiatury
//TODO: motyw kolorystyczny, zapisanie wszystkich kolorów w Config lub w xml
//TODO: ustalenie marginesów w layoutach i wypozycjonowanie elementów
//TODO: konfiguracja: wyświetlacz zawsze zapalony, wielkość czcionki, marginesy między elementami
//TODO: pole edycyjne multiline przy overflow
//TODO: ikona aplikacji

public class App extends BaseApp implements GUIListener {

    TreeManager treeManager;
    GUI GUI;

    AppState state;

    public App(AppCompatActivity activity) {
        super(activity);

        preferences.preferencesLoad();

        treeManager = new TreeManager();
        treeManager.loadRootTree(files, preferences);

        GUI = new GUI(activity, this);
        GUI.showItemsList(treeManager.getCurrentItem());
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
        showInfo(info, GUI.getMainContent());
    }



    public void addNewItem() {
        TreeItem newItem = treeManager.getCurrentItem().add("");
        editItem(newItem, treeManager.getCurrentItem());
    }

    private void editItem(TreeItem item, TreeItem parent) {
        treeManager.setEditItem(item);
        GUI.showEditItemPanel(item, parent);
        state = AppState.EDIT_ITEM_CONTENT;
    }

    private void discardEditingItem(){
        treeManager.setEditItem(null);
        GUI.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Anulowano edycję elementu.");
    }

    public void saveItem(TreeItem editItem, String content) {
        editItem.setContent(content);
        GUI.showItemsList(treeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        showInfo("Zapisano element");
    }

    private void removeItem(int position) {
        treeManager.getCurrentItem().remove(position);
        showInfo("Element zostal usunięty.");
        GUI.updateItemsList(treeManager.getCurrentItem());
    }

    public void goUp() {
        try {
            treeManager.goUp();
            GUI.updateItemsList(treeManager.getCurrentItem());
        } catch (NoSuperItemException e) {
            quit();
        }
    }

    private void backClicked(){
        if(state == AppState.ITEMS_LIST) {
            goUp();
        }else if(state == AppState.EDIT_ITEM_CONTENT) {
            discardEditingItem();
        }
    }



    @Override
    public void onToolbarBackClick() {
        backClicked();
    }

    @Override
    public void onAddItemButtonClicked() {
        addNewItem();
    }

    @Override
    public void onTreeItemClicked(int position, TreeItem item) {
        treeManager.goInto(position);
        GUI.updateItemsList(treeManager.getCurrentItem());
    }

    @Override
    public void onTreeItemEditClicked(int position, TreeItem item) {
        editItem(item, treeManager.getCurrentItem());
    }

    @Override
    public void onTreeItemRemoveClicked(int position, TreeItem item) {
        removeItem(position);
    }

    @Override
    public void onSavedEditedItem(TreeItem editedItem, String content) {
        saveItem(editedItem, content);
    }
}
