
package com.android.theguardian;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<String> getSelectedTags(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_TAG, Context.MODE_PRIVATE);
        int size = sharedPreferences.getInt("size", 4);
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            titles.add(sharedPreferences.getString(String.valueOf(i), ""));
        }
        return titles;
    }

    public static List<String> getUnselectedTags(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_TAG, Context.MODE_PRIVATE);
        int size = sharedPreferences.getInt("size", 4);
        List<String> titles = new ArrayList<>();
        for (int i = size; i < MainActivity.ALL_TAGS_SIZE; i++) {
            titles.add(sharedPreferences.getString(String.valueOf(i), ""));
        }
        return titles;
    }
}

