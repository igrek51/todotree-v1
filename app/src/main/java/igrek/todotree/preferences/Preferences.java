package igrek.todotree.preferences;

import android.app.Activity;

import igrek.todotree.output.Output;

//  KONFIGURACJA
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: konfiguracja: wyświetlacz zawsze zapalony, wielkość czcionki, marginesy między elementami

//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza

public class Preferences extends BasePreferences {

    public String dbFilePath = "Android/data/igrek.todotree/todo.dat";

    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {
        setString("dbFilePath", dbFilePath);
    }

    public void preferencesLoad() {
        if (exists("dbFilePath")) {
            dbFilePath = getString("dbFilePath");
            Output.info("Wczytano ścieżkę do pliku bazy: " + dbFilePath);
        } else {
            Output.info("Wczytano domyślną ścieżkę do pliku bazy: " + dbFilePath);
        }
    }
}
