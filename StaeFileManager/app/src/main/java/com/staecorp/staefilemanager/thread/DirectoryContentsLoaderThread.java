package com.staecorp.staefilemanager.thread;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.common.collect.Ordering;
import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;
import com.staecorp.staefilemanager.R;
import com.staecorp.staefilemanager.model.FileItem;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryContentsLoaderThread extends Thread {
    private URI uri;
    private List<FileItem> fileItemsArray;
    private FileManagerActivity.SortModes sortMode;
    private boolean shouldUpdateUI=false;

    public DirectoryContentsLoaderThread(URI uri, List<FileItem> fileItemsArray) {
        this.uri = uri;
        this.fileItemsArray = fileItemsArray;
    }

    @Override
    public void run() {
        super.run();
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
                        fileItem = createFileItem(f);
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
                AppState.instance().getFileManagerActivity().showErrorDialog("Error opening path: Can not read current directory");
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

    private FileItem createFileItem(File file)
    {
        FileItem fileItem=new FileItem(file.getName());
        fileItem.setUri(file.toURI());

        if(file.getParentFile()!=null)
        {
            fileItem.setParentURI(file.getParentFile().toURI());
        }
        if(file.isDirectory())
        {
            fileItem.setIcon(AppCompatResources.getDrawable(AppState.instance().getFileManagerActivity(), R.drawable.folder));
        }
        else
        {
            fileItem.setIcon(AppCompatResources.getDrawable(AppState.instance().getFileManagerActivity(), R.drawable.file));
        }
        return fileItem;
    }

    private void sortFileItems()
    {
        switch(sortMode)
        {
            case NAME:
                Collections.sort(fileItemsArray,(fi1,fi2)->{
                    File fi1File,fi2File;
                    fi1File=new File(fi1.getUri());
                    fi2File=new File(fi2.getUri());
                    if(fi1File.isDirectory() && fi2File.isDirectory())
                    {
                        return fi1.getName().toLowerCase().compareTo(fi2.getName().toLowerCase());
                    }
                    if(fi1File.isFile() && fi2File.isFile())
                    {
                        return fi1.getName().toLowerCase().compareTo(fi2.getName().toLowerCase());
                    }
                    if(fi1File.isDirectory() && fi2File.isFile())
                    {
                        return -1;
                    }
                    if(fi1File.isFile() && fi2File.isDirectory())
                    {
                        return 1;
                    }
                    return 0;
                });
                break;
            case DATE:
                break;
        }
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
}
