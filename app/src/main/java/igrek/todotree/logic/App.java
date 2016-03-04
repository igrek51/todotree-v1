package igrek.todotree.logic;

import android.support.v7.app.AppCompatActivity;

public class App extends BaseApp {

    public App(AppCompatActivity activity) {
        super(activity);
        preferences.preferencesLoad();
    }

}
