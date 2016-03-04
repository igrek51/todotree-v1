package igrek.todotree.settings.preferences;

import android.app.Activity;

public class Preferences extends BasePreferences {
    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {
        //TODO: zapisanie do shared preferences
//        setString("samplesPath", app.samplesPath);
    }

    public void preferencesLoad() {
        //TODO: wczytanie z shared preferences
//        if (exists("samplesPath")) {
//            app.samplesPath = getString("samplesPath");
//            Output.info("Wczytano ścieżkę wzorców: " + app.samplesPath);
//        } else {
//            Output.info("Wczytano domyślną ścieżkę wzorców: " + app.samplesPath);
//        }
    }
}
