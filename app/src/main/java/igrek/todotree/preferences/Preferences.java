package igrek.todotree.preferences;

import android.app.Activity;

import igrek.todotree.logger.Logs;
import igrek.todotree.logic.controller.services.IService;

//  KONFIGURACJA
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh
//TODO: konfiguracja: wyświetlacz zawsze zapalony, wielkość czcionki, marginesy między elementami

//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza

public class Preferences extends BasePreferences implements IService {

    public String dbFilePath = "Android/data/igrek.todotree/todo.dat";

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
