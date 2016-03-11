package igrek.todotree;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import igrek.todotree.logic.app.App;
import igrek.todotree.system.output.Output;

public class MainActivity extends AppCompatActivity {

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            app = new App(this);
        } catch (Exception ex) {
            Output.errorCritical(this, ex);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            app.resizeEvent(newConfig.screenWidthDp, newConfig.screenHeightDp);
            Output.log("orientation changed: landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            app.resizeEvent(newConfig.screenWidthDp, newConfig.screenHeightDp);
            Output.log("orientation changed: portrait");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.resume();
    }

    @Override
    protected void onDestroy() {
//        app.quit();
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return app.optionsSelect(item.getItemId()) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (app.keycodeBack()) return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (app.keycodeMenu()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        /*
        //przechwycenie klawisza menu
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        */
        return super.onKeyDown(keyCode, event);
    }
}


