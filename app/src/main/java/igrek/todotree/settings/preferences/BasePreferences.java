package igrek.todotree.settings.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import igrek.todotree.settings.Config;

public class BasePreferences {
    SharedPreferences settings;

    public BasePreferences(Activity activity) {
        settings = activity.getApplicationContext().getSharedPreferences(Config.shared_preferences_name, Context.MODE_PRIVATE);
    }

    public void clear() {
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }

    public void setBoolean(String name, boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public void setInt(String name, int value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public void setString(String name, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public boolean getBoolean(String name, boolean _default) {
        return settings.getBoolean(name, _default);
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public int getInt(String name, int _default) {
        return settings.getInt(name, _default);
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public String getString(String name, String _default) {
        return settings.getString(name, _default);
    }

    public String getString(String name) {
        return getString(name, "");
    }

    public boolean exists(String name) {
        return settings.contains(name);
    }
}
