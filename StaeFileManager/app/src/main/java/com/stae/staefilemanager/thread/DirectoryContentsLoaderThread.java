package com.stae.staefilemanager.thread;

import androidx.appcompat.content.res.AppCompatResources;

import com.stae.staefilemanager.AppState;
import com.stae.staefilemanager.R;
import com.stae.staefilemanager.model.FileItem;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DirectoryContentsLoaderThread extends Thread {
    private URI uri;
    private List<FileItem> fileItemsArray;
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
        if(file.isDirectory())
        {
            File[] files=file.listFiles();
            if(files!=null)
            {

                for(File f:files)
                {
                    fileItem=createFileItem(f);
                    fileItemsArray.add(fileItem);
                }
            }
        }
        if(shouldUpdateUI)
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

    public void shouldUpdateUI() {
        shouldUpdateUI = true;
    }

    public void shouldNotUpdateUI()
    {
        shouldUpdateUI = false;
    }
}
