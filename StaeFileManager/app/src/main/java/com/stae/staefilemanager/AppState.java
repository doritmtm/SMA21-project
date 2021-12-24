package com.stae.staefilemanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.net.URI;

public class AppState {
    private static AppState singleton;
    private static Context context;
    private SharedPreferences preferences;
    private FileManagerActivity fileManagerActivity;
    private URI currentDir;
    public AppState()
    {
        preferences=context.getSharedPreferences("StaeFileManagerPref",Context.MODE_PRIVATE);
    }
    public static AppState instance()
    {
        if(singleton==null)
        {
            singleton=new AppState();
        }
        return singleton;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AppState.context = context;
    }

    public FileManagerActivity getFileManagerActivity() {
        return fileManagerActivity;
    }

    public void setFileManagerActivity(FileManagerActivity fileManagerActivity) {
        this.fileManagerActivity = fileManagerActivity;
    }

    public URI getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(URI currentDir) {
        this.currentDir = currentDir;
    }
}
