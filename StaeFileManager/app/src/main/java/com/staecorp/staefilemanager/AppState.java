package com.staecorp.staefilemanager;

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

    public static String filesizeDisplayString(long bytes)
    {
        if(0 <= bytes && bytes <= 1023)
        {
            return bytes+" B";
        }
        if(1024 <= bytes && bytes <= 1048575)
        {
            return String.format("%,.2f",(float)bytes/1024.0)+" KB";
        }
        if(1048576 <= bytes && bytes <= 1073741823)
        {
            return String.format("%,.2f",(float)bytes/1048576.0)+" MB";
        }
        if(1073741824 <= bytes)
        {
            return String.format("%,.2f",(float)bytes/1073741824.0)+" GB";
        }
        return "";
    }
}
