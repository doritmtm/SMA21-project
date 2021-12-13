package com.stae.staefilemanager.model;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.stae.staefilemanager.AppState;
import com.stae.staefilemanager.R;
import com.stae.staefilemanager.adapter.FileRecyclerViewAdapter;

import java.net.URI;

public class FileItem {
    private String name;
    private Drawable icon;
    private URI uri,parentURI;
    private boolean checked;
    public FileItem(String name) {
        this.name = name;
        this.icon= AppCompatResources.getDrawable(AppState.getContext(),R.drawable.file);
    }

    public FileItem(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
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

}
