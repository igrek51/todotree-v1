package igrek.todotree.services.preferences;

import android.app.Activity;

import igrek.todotree.logger.Logs;

//  KONFIGURACJA
//TODO: ekran konfiguracji
//TODO: konfiguracja położenia pliku z bazą dancyh

//TODO: shared preferences: zautomatyzowanie w celu konfiguracji, definicja: typ, nazwa, wartość domyślna, refleksja, automatyczny zapis, odczyt, generowanie fomrularza

public class Preferences extends BasePreferences {
	
	public String dbFilePath = "Android/data/igrek.todotree/todo.json";
	
	public Preferences(Activity activity) {
		super(activity);
		loadAll();
	}
	
	public void saveAll() {
		if (dbFilePath != null) {
			setString("dbFilePath", dbFilePath);
			Logs.debug("Shared preferences saved.");
		}
	}
	
	private void loadAll() {
		if (exists("dbFilePath")) {
			dbFilePath = getString("dbFilePath");
			Logs.debug("Database path loaded: " + dbFilePath);
		} else {
			Logs.debug("Default database path loaded: " + dbFilePath);
		}
	}
}
