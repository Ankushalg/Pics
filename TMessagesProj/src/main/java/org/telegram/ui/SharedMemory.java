package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

class SharedMemory {
    private SharedPreferences prefs;
    SharedMemory(Context ctx) {
        prefs = ctx.getSharedPreferences("PICS_MEMORY", Context.MODE_PRIVATE);
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

    void setFileTask(Set<String> value) {
        prefs.edit().putStringSet("FileTask", value).apply();
    }
    Set<String> getFileTask() {
        return prefs.getStringSet("FileTask",null);
    }

//    void setRunningFileTask(Set<String> value) { prefs.edit().putStringSet("RunningFileTask", value).apply(); }
//    Set<String> getRunningFileTask() {
//        return prefs.getStringSet("RunningFileTask",null);
//    }

//    void setErrorFileTask(Set<String> value) { prefs.edit().putStringSet("ErrorFileTask", value).apply(); }
//    Set<String> getErrorFileTask() {
//        return prefs.getStringSet("ErrorFileTask",null);
//    }

    void setIsFOR(int value) { prefs.edit().putInt("IsFOR", value).apply(); }
    int getIsFOR() {
        return prefs.getInt("IsFOR",0);
    }

    void setCFileAction(int value) { prefs.edit().putInt("CFileAction", value).apply(); }
    int getCFileAction() {
        return prefs.getInt("CFileAction",0);
    }

    void setCFileDestination(String value) { prefs.edit().putString("CFileDestination", value).apply(); }
    String getCFileDestination() {
        return prefs.getString("CFileDestination","");
    }

    void setCFilePath(String value) { prefs.edit().putString("CFilePath", value).apply(); }
    String getCFilePath() {
        return prefs.getString("CFilePath","");
    }

    void setCFileProgressTwo(int value) { prefs.edit().putInt("CFileProgressTwo", value).apply(); }
    int getCFileProgressTwo() {
        return prefs.getInt("CFileProgressTwo",0);
    }

    void setCFileProgressThree(int value) { prefs.edit().putInt("CFileProgressThree", value).apply(); }
    int getCFileProgressThree() {
        return prefs.getInt("CFileProgressThree",0);
    }

    void setCFileProgress(int value) { prefs.edit().putInt("CFileProgress", value).apply(); }
    int getCFileProgress() {
        return prefs.getInt("CFileProgress",0);
    }

//    void setAsyncTaskRunning(boolean value) { prefs.edit().putBoolean("AsyncTaskRunning", value).apply(); }
//    boolean getAsyncTaskRunning() {
//        return prefs.getBoolean("AsyncTaskRunning",false);
//    }



}