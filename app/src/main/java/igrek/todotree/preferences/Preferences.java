package igrek.todotree.preferences;

import android.app.Activity;

import igrek.todotree.logger.Logs;

//  KONFIGURACJA
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh

//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza

public class Preferences extends BasePreferences {
    
    public String dbFilePath = "Android/data/igrek.todotree2/todo.dat";

    public Preferences(Activity activity){
        super(activity);
        loadAll();
    }

    public void saveAll() {
		if(dbFilePath != null){
 	       setString("dbFilePath", dbFilePath);
		}
    }

    public void loadAll() {
        if (exists("dbFilePath")) {
            dbFilePath = getString("dbFilePath");
            Logs.debug("Wczytano ścieżkę do pliku bazy: " + dbFilePath);
        } else {
            Logs.debug("Wczytano domyślną ścieżkę do pliku bazy: " + dbFilePath);
        }
    }
}
