package com.staecorp.staefilemanager.thread;

import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;

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
        int i=0;
        showProgress(i);
        URI currentDir=AppState.instance().getCurrentDir();
        try {
            if (fileOperation == FileManagerActivity.FileOperations.COPY)
            {
                for (File f : filesSelected)
                {
                    if (f.isDirectory())
                    {
                        FileUtils.copyDirectory(f, new File(currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(f.getName()))));
                    }
                    else
                    {
                        FileUtils.copyFile(f, new File(currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(f.getName()))));
                    }
                    i++;
                    showProgress(i);
                }
            }
            if (fileOperation == FileManagerActivity.FileOperations.CUT)
            {
                for (File f : filesSelected)
                {
                    Files.move(f,new File(currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(f.getName()))));
                    i++;
                    showProgress(i);
                }
            }
            if (fileOperation == FileManagerActivity.FileOperations.DELETE)
            {
                for (File f : filesSelected)
                {
                    if (f.isDirectory()) {

                        FileUtils.deleteDirectory(f);
                    }
                    else
                    {
                        FileUtils.forceDelete(f);
                    }
                    i++;
                    showProgress(i);
                }
            }
            fileOperation=FileManagerActivity.FileOperations.NOOP;
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                AppState.instance().getFileManagerActivity().loadDirectoryContentsAndUpdateUI(currentDir);
            });
        }
        catch(IOException e)
        {
            AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
                AppState.instance().getFileManagerActivity().showErrorDialog(e.getMessage());
            });
            e.printStackTrace();
        }
    }

    public FileManagerActivity.FileOperations getFileOperation() {
        return fileOperation;
    }

    public void setFileOperation(FileManagerActivity.FileOperations fileOperation) {
        this.fileOperation = fileOperation;
    }

    private void showProgress(int i)
    {
        AppState.instance().getFileManagerActivity().runOnUiThread(() -> {
            AppState.instance().getFileManagerActivity().showProgressMessage("Processing ("+i+"/"+filesSelected.size()+")...");
        });
    }

}
