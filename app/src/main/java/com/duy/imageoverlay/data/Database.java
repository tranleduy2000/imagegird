package com.duy.imageoverlay.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Duy on 17-Feb-17.
 */

public class Database {
    public static final String MODE_POSITION = "MODE_POSITION";
    public static final String INTERVAL_POSITION = "INTERVAL_POSITION";
    public static final String DRAW_VERTICAL_LINE = "DRAW_VERTICAL_LINE";
    public static final String DRAW_HORIZONTAL_LINE = "DRAW_VERTICAL_LINE";
    public static final String LASTEST_FILE = "LASTEST_FILE";
    public static final String COLOR_LINE = "COLOR_LINE";
    public static final String PAINT_SIZE_POSITION = "PAINT_SIZE_POSITION";
    public static final String STARTED = "STARTED";
    public static final String COLOR_BACKGROUND = "COLOR_BACKGROUND";
    public static final String FILTER_TYPE = "FILTER_TYPE";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Context context;


    public Database(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        this.context = context;
    }

    public boolean get(String key, Boolean default_) {
        return sharedPreferences.getBoolean(key, default_);
    }

    public int getInt(String key, int default_) {
        return sharedPreferences.getInt(key, default_);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void putBool(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public String getString(String key) {

        return sharedPreferences.getString(key, "");
    }

    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

}
