package igrek.todotree.settings;

import android.view.WindowManager;

//TODO: wyjebać i przenieść do odpowiedzialnych klas

public class Config {
    //STAŁE
    //  OUTPUT
    public static class Output {
        public static final String logTag = "ylog";
        public static final int echo_showtime = 1800; //[ms]
        public static final boolean show_exceptions_trace = true;
    }
    //  SCREEN
    public static class Screen {
        public static final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        public static final boolean fullscreen = false;
        public static final boolean hide_taskbar = true;
        public static final boolean keep_screen_on = true;
    }
    //  USTAWIENIA UŻYTKOWNIKA
    public static final String shared_preferences_name = "ToDoTreeUserPreferences";
    //  BACKUP
    public static final String backup_file_prefix = "backup_";
    public static final int backup_num = 10;
}
