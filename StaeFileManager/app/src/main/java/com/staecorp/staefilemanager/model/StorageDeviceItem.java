package com.staecorp.staefilemanager.model;

import android.graphics.drawable.Drawable;

import java.net.URI;

public class StorageDeviceItem {
    private URI mountPath;
    private Drawable icon;
    private String title;
    private long freeBytes,totalBytes,usedBytes;
    private String freeGB,totalGB,usedGB;
    private int percentageUsed; //multiplied by 100

    public URI getMountPath() {
        return mountPath;
    }

    public void setMountPath(URI mountPath) {
        this.mountPath = mountPath;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getFreeBytes() {
        return freeBytes;
    }

    public void setFreeBytes(long freeBytes) {
        this.freeBytes = freeBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public String getFreeGB() {
        return freeGB;
    }

    public void setFreeGB(String freeGB) {
        this.freeGB = freeGB;
    }

    public String getTotalGB() {
        return totalGB;
    }

    public void setTotalGB(String totalGB) {
        this.totalGB = totalGB;
    }

    public String getUsedGB() {
        return usedGB;
    }

    public void setUsedGB(String usedGB) {
        this.usedGB = usedGB;
    }

    public int getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(int percentageUsed) {
        this.percentageUsed = percentageUsed;
    }
}
