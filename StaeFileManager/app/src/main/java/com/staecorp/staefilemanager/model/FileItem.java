package com.staecorp.staefilemanager.model;

import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;
import com.staecorp.staefilemanager.R;
import com.staecorp.staefilemanager.adapter.FileRecyclerViewAdapter;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileItem {
    private String name,detail;
    private Drawable icon;
    private URI uri,parentURI;
    private File file;
    private boolean checked;
    private FileRecyclerViewAdapter.ViewHolder viewHolder;
    private long lastModified;
    private long length;
    private boolean isDirectory,isFile;
    private FileManagerActivity.DetailModes detailMode;
    public FileItem(File file, FileManagerActivity.DetailModes detailMode) {
        this.file=file;
        this.detailMode=detailMode;
        name=file.getName();
        uri=file.toURI();
        if(file.getParentFile()!=null)
        {
            parentURI=file.getParentFile().toURI();
        }
        isDirectory=file.isDirectory();
        isFile=file.isFile();
        lastModified=file.lastModified();
        length=file.length();
        if(file.isDirectory())
        {
            icon=AppCompatResources.getDrawable(AppState.instance().getFileManagerActivity(), R.drawable.folder);
            switch(detailMode)
            {
                case DATE:
                    Date date=new Date(file.lastModified());
                    DateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    detail=dateFormat.format(date);
                    break;
                default:
                    detail="";
                    break;
            }
        }
        else
        {
            icon=AppCompatResources.getDrawable(AppState.instance().getFileManagerActivity(), R.drawable.file);
            switch(detailMode)
            {
                case SIZE:
                    detail=AppState.filesizeDisplayString(file.length());
                    break;
                case DATE:
                    Date date=new Date(file.lastModified());
                    DateFormat dateFormat=new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    detail=dateFormat.format(date)+"\n"+AppState.filesizeDisplayString(file.length());
                    break;
                default:
                    detail="";
                    break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getParentURI() {
        return parentURI;
    }

    public void setParentURI(URI parentURI) {
        this.parentURI = parentURI;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public FileRecyclerViewAdapter.ViewHolder getViewHolder() {
        return viewHolder;
    }

    public void setViewHolder(FileRecyclerViewAdapter.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public FileManagerActivity.DetailModes getDetailMode() {
        return detailMode;
    }

    public void setDetailMode(FileManagerActivity.DetailModes detailMode) {
        this.detailMode = detailMode;
    }
}
