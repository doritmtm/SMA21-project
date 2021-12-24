package com.stae.staefilemanager.thread;

import com.stae.staefilemanager.AppState;
import com.stae.staefilemanager.FileManagerActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class FileOperationThread extends Thread {
    private FileManagerActivity.FileOperations fileOperation=FileManagerActivity.FileOperations.NOOP;
    private List<File> filesSelected;

    public FileOperationThread(List<File> filesSelected) {
        this.filesSelected = filesSelected;
    }

    @Override
    public void run() {
        super.run();
        URI currentDir=AppState.instance().getCurrentDir();
        if(fileOperation==FileManagerActivity.FileOperations.COPY)
        {
            for(File f:filesSelected)
            {
                try {
                    if(f.isDirectory())
                    {
                        FileUtils.copyDirectory(f,new File(currentDir.resolve(f.getName())));
                    }
                    else
                    {
                        FileUtils.copyFile(f,new File(currentDir.resolve(f.getName())));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(fileOperation==FileManagerActivity.FileOperations.CUT)
        {
            for(File f:filesSelected)
            {
                try {
                    if(f.isDirectory())
                    {
                        FileUtils.copyDirectory(f,new File(currentDir.resolve(f.getName())));
                        FileUtils.deleteDirectory(f);
                    }
                    else
                    {
                        FileUtils.copyFile(f,new File(currentDir.resolve(f.getName())));
                        FileUtils.forceDelete(f);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(fileOperation==FileManagerActivity.FileOperations.DELETE)
        {
            for(File f:filesSelected)
            {
                try {
                    if(f.isDirectory())
                    {
                        FileUtils.deleteDirectory(f);
                    }
                    else
                    {
                        FileUtils.forceDelete(f);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        fileOperation=FileManagerActivity.FileOperations.NOOP;
        AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
            AppState.instance().getFileManagerActivity().loadDirectoryContentsAndUpdateUI(currentDir);
        });

    }

    public FileManagerActivity.FileOperations getFileOperation() {
        return fileOperation;
    }

    public void setFileOperation(FileManagerActivity.FileOperations fileOperation) {
        this.fileOperation = fileOperation;
    }

}
