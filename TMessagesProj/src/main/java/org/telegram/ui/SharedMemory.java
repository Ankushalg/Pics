package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

class SharedMemory {
    private SharedPreferences prefs;
    SharedMemory(Context ctx) {
        prefs = ctx.getSharedPreferences("PICS_MEMORY", Context.MODE_PRIVATE);
    }

    void setFileTask(Set<String> value) {
        prefs.edit().putStringSet("FileTask", value).apply();
    }
    Set<String> getFileTask() {
        return prefs.getStringSet("FileTask",null);
    }

    void setRunningFileTask(Set<String> value) { prefs.edit().putStringSet("RunningFileTask", value).apply(); }
    Set<String> getRunningFileTask() {
        return prefs.getStringSet("RunningFileTask",null);
    }

    void setFileAction(int value) {
        prefs.edit().putInt("FileAction", value).apply();
    }
    int getFileAction() {
        return prefs.getInt("FileAction",0);
    }

    void setFileDestination(String value) { prefs.edit().putString("FileDestination", value).apply(); }
    String getFileDestination() {
        return prefs.getString("FileDestination","");
    }

}