package igrek.todotree.logic.app;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import igrek.todotree.settings.Config;
import igrek.todotree.settings.preferences.Preferences;
import igrek.todotree.system.files.Files;
import igrek.todotree.system.output.Output;
import igrek.todotree.system.touchscreen.ITouchScreenController;

public abstract class BaseApp implements ITouchScreenController {

    public AppCompatActivity activity;
    private Thread.UncaughtExceptionHandler defaultUEH;

    boolean running = true;

    public Files files;
    public Preferences preferences;

    public BaseApp(AppCompatActivity aActivity) {
        this.activity = aActivity;

        new Config();

        //łapanie niezłapanych wyjątków
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable th) {
                Output.errorUncaught(th);
                //przekazanie dalej do systemu operacyjnego
                defaultUEH.uncaughtException(thread, th);
            }
        });

        //schowanie paska tytułu
        if (Config.Screen.hide_taskbar) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
        //fullscreen
        if (Config.Screen.fullscreen) {
            activity.getWindow().setFlags(Config.Screen.fullscreen_flag, Config.Screen.fullscreen_flag);
        }
        if (Config.Screen.keep_screen_on) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        new Output();
        files = new Files(activity);
        preferences = new Preferences(activity);

        //        activity.setContentView(graphics);

        Output.log("Inicjalizacja aplikacji...");
    }

    public void pause() {

    }

    public void resume() {

    }

    public void quit() {
        if (!running) { //próba ponownego zamknięcia
            Output.error("Zamykanie - próba ponownego zamknięcia");
            return;
        }
        Output.log("Zamykanie aplikacji...");
        running = false;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.finish();
    }

    @Override
    public void touchDown(float touch_x, float touch_y) {

    }

    @Override
    public void touchMove(float touch_x, float touch_y) {

    }

    @Override
    public void touchUp(float touch_x, float touch_y) {

    }

    public void resizeEvent(int w, int h) {
        Output.log("Rozmiar ekranu zmieniony na: " + w + "px x " + h + "px");
    }

    public boolean keycodeBack() {
        quit();
        return true;
    }

    public boolean keycodeMenu() {
        return false;
    }

    public boolean optionsSelect(int id) {
        return false;
    }

    public void minimize() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(startMain);
    }

    public void showInfo(String info, View view){
        Snackbar.make(view, info, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        Output.info(info);
    }
}
