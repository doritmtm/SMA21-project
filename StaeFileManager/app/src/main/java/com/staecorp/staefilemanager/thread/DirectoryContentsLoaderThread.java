package com.staecorp.staefilemanager.thread;

import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;
import com.staecorp.staefilemanager.R;
import com.staecorp.staefilemanager.model.FileItem;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DirectoryContentsLoaderThread extends Thread {
    private URI uri;
    private List<FileItem> fileItemsArray;
    private FileManagerActivity.SortModes sortMode;
    private FileManagerActivity.DetailModes detailMode;
    private boolean shouldUpdateUI=false;

    public DirectoryContentsLoaderThread(URI uri, List<FileItem> fileItemsArray) {
        this.uri = uri;
        this.fileItemsArray = fileItemsArray;
    }

    @Override
    public void run() {
        super.run();
        if(shouldUpdateUI)
        {
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                AppState.instance().getFileManagerActivity().showProgressMessage("Processing...");
            });
        }
        File file=new File(uri);
        FileItem fileItem;
        if(file.canRead())
        {
            if (file.isDirectory())
            {
                File[] files = file.listFiles();
                if (files != null)
                {

                    for (File f : files)
                    {
                        fileItem = new FileItem(f,detailMode);
                        fileItemsArray.add(fileItem);
                    }
                }
                else
                {
                    AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                        AppState.instance().getFileManagerActivity().showErrorDialog("Error opening path");
                    });
                }
            }
        }
        else
        {
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                AppState.instance().getFileManagerActivity().showErrorDialog("Error opening path: Cannot read current directory");
            });
        }
        sortFileItems();
        if (shouldUpdateUI)
        {
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                AppState.instance().getFileManagerActivity().updateDirectoryContentsUI();
            });
        }
    }

    private void sortFileItems()
    {
        List<FileItem> tempFileItemArray=new ArrayList<FileItem>();
        tempFileItemArray.addAll(fileItemsArray);
        try {
            switch (sortMode) {
                case NAME:
                    Collections.sort(tempFileItemArray, (fi1, fi2) -> {
                        if (fi1.isDirectory() && fi2.isDirectory()) {
                            return fi1.getName().toLowerCase().compareTo(fi2.getName().toLowerCase());
                        }
                        if (fi1.isFile() && fi2.isFile()) {
                            return fi1.getName().toLowerCase().compareTo(fi2.getName().toLowerCase());
                        }
                        if (fi1.isDirectory() && fi2.isFile()) {
                            return -1;
                        }
                        if (fi1.isFile() && fi2.isDirectory()) {
                            return 1;
                        }
                        return 0;
                    });
                    break;
                case DATE:
                    Collections.sort(tempFileItemArray, (fi1, fi2) -> {
                        if (fi1.isDirectory() && fi2.isDirectory()) {
                            if (fi1.getLastModified() - fi2.getLastModified() < 0) {
                                return 1;
                            }
                            if (fi1.getLastModified() - fi2.getLastModified() == 0) {
                                return 0;
                            }
                            if (fi1.getLastModified() - fi2.getLastModified() > 0) {
                                return -1;
                            }
                        }
                        if (fi1.isFile() && fi2.isFile()) {
                            if (fi1.getLastModified() - fi2.getLastModified() < 0) {
                                return 1;
                            }
                            if (fi1.getLastModified() - fi2.getLastModified() == 0) {
                                return 0;
                            }
                            if (fi1.getLastModified() - fi2.getLastModified() > 0) {
                                return -1;
                            }
                        }
                        if (fi1.isDirectory() && fi2.isFile()) {
                            return -1;
                        }
                        if (fi1.isFile() && fi2.isDirectory()) {
                            return 1;
                        }
                        return 0;
                    });
                    break;
                case SIZE:
                    Collections.sort(tempFileItemArray, (fi1, fi2) -> {
                        if (fi1.isDirectory() && fi2.isDirectory()) {
                            return fi1.getName().toLowerCase().compareTo(fi2.getName().toLowerCase());
                        }
                        if (fi1.isFile() && fi2.isFile()) {
                            if (fi1.getLength() - fi2.getLength() < 0) {
                                return 1;
                            }
                            if (fi1.getLength() - fi2.getLength() == 0) {
                                return 0;
                            }
                            if (fi1.getLength() - fi2.getLength() > 0) {
                                return -1;
                            }
                        }
                        if (fi1.isDirectory() && fi2.isFile()) {
                            return -1;
                        }
                        if (fi1.isFile() && fi2.isDirectory()) {
                            return 1;
                        }
                        return 0;
                    });
                    break;
            }
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        fileItemsArray.clear();
        fileItemsArray.addAll(tempFileItemArray);
    }

    public void shouldUpdateUI() {
        shouldUpdateUI = true;
    }

    public void shouldNotUpdateUI()
    {
        shouldUpdateUI = false;
    }

    public FileManagerActivity.SortModes getSortMode() {
        return sortMode;
    }

    public void setSortMode(FileManagerActivity.SortModes sortMode) {
        this.sortMode = sortMode;
    }

    public FileManagerActivity.DetailModes getDetailMode() {
        return detailMode;
    }

    public void setDetailMode(FileManagerActivity.DetailModes detailMode) {
        this.detailMode = detailMode;
    }
}
