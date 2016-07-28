package igrek.todotree.preferences;

import android.app.Activity;

import igrek.todotree.output.Output;

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
