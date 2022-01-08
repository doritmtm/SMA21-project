package com.staecorp.staefilemanager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.net.UrlEscapers;

import java.net.URI;

public class AppState {
    private static AppState singleton;
    private static Context context;
    private SharedPreferences preferences;
    private FileManagerActivity fileManagerActivity;
    private URI currentDir;
    private FileManagerActivity.SortModes sortMode=FileManagerActivity.SortModes.NAME;
    private boolean somethingInClipboard=false;
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

    public FileManagerActivity.SortModes getSortMode() {
        return sortMode;
    }

    public void setSortMode(FileManagerActivity.SortModes sortMode) {
        this.sortMode = sortMode;
    }

    public static FileManagerActivity.SortModes sortModeTranslated(String s)
    {
        switch(s)
        {
            case "NOOP": return FileManagerActivity.SortModes.NOOP;
            case "NAME": return FileManagerActivity.SortModes.NAME;
            case "DATE": return FileManagerActivity.SortModes.DATE;
            case "SIZE": return FileManagerActivity.SortModes.SIZE;
        }
        return FileManagerActivity.SortModes.NOOP;
    }

    public boolean isSomethingInClipboard() {
        return somethingInClipboard;
    }

    public void setSomethingInClipboard(boolean somethingInClipboard) {
        this.somethingInClipboard = somethingInClipboard;
    }

    public static String escapePath(String path)
    {
        String[] fragments=path.split("/");
        String result="";
        for(String f:fragments)
        {
            f=UrlEscapers.urlPathSegmentEscaper().escape(f);
            result+=f+"/";
        }
        return result;
    }
}
