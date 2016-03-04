package igrek.todotree.system.output;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import igrek.todotree.settings.Config;

public class Output {
    public static void reset() {
        echos = "";
    }

    //  LOG
    public static void log(String l) {
        Log.i(Config.Output.logTag, l);
    }

    public static void logError(String l) {
        Log.e(Config.Output.logTag, l);
    }

    public static void log(String l, int i) {
        log(l + " = " + i);
    }

    public static void log(String l, float f) {
        log(l + " = " + f);
    }

    //  ERRORS, EXCEPTIONS
    public static void error(String e) {
        echoMultiline("[ERROR] " + e);
        logError("[ERROR] " + e);
    }

    public static void error(Exception ex) {
        if (ex instanceof SoftErrorException) {
            echoMultiline("[BŁĄD] " + ex.getMessage());
        } else {
            echoMultiline("[" + ex.getClass().getName() + "] " + ex.getMessage());
        }
        logError("[EXCEPTION - " + ex.getClass().getName() + "] " + ex.getMessage());
        if(Config.Output.show_exceptions_trace){
            printStackTrace(ex);
        }
    }

    public static void printStackTrace(Exception ex){
        logError(Log.getStackTraceString(ex));
    }

    public static void errorThrow(String e) throws SoftErrorException {
        throw new SoftErrorException(e);
    }

    public static void errorCritical(String e) throws Exception {
//        if (App.geti().engine == null || App.geti().engine.activity == null) {
//            error("errorCritical: Brak activity");
//            return;
//        }
//        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(App.geti().engine.activity);
//        dlgAlert.setMessage(e);
//        dlgAlert.setTitle("Błąd krytyczny");
//        dlgAlert.setPositiveButton("Zamknij", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                if (App.geti().engine != null) {
//                    App.geti().engine.quit();
//                }
//            }
//        });
//        dlgAlert.setCancelable(false);
//        dlgAlert.create().show();
        logError("[CRITICAL ERROR] " + e);
        throw new Exception(e);
    }

    //  INFO, ECHO

    public static void info(String e) {
        echoMultiline(e);
        log("[info] " + e);
    }

    private static void echoMultiline(String e) {
        if (echos.length() == 0) {
            echos = e;
            lastEcho = System.currentTimeMillis();
        } else {
            echos += "\n" + e;
        }
    }

    public static void echoClear1AfterDelay() {
        if (System.currentTimeMillis() > lastEcho + Config.Output.echo_showtime) {
            echoClear1Line();
            lastEcho += Config.Output.echo_showtime;
        }
    }

    public static void echoClear1Line() {
        if (echos.length() == 0) return;
        //usuwa 1 wpis z echo
        int firstIndex = echos.indexOf("\n");
        if (firstIndex == -1) {
            echos = "";
        } else {
            echos = echos.substring(firstIndex + 1);
        }
    }

    public static void echoWait(int waitms){
        lastEcho = System.currentTimeMillis() + waitms;
    }

    public static String echos = "";
    public static long lastEcho = 0;
}
