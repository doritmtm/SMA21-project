package com.staecorp.staefilemanager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.staecorp.staefilemanager.adapter.FileRecyclerViewAdapter;
import com.staecorp.staefilemanager.adapter.StorageDeviceRecyclerViewAdapter;
import com.staecorp.staefilemanager.model.FileItem;
import com.staecorp.staefilemanager.model.StorageDeviceItem;
import com.staecorp.staefilemanager.thread.DirectoryContentsLoaderThread;
import com.staecorp.staefilemanager.thread.FileOperationThread;
import com.staecorp.staefilemanager.ui.CustomRecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileManagerActivity extends AppCompatActivity {
    private CustomRecyclerView fileRecyclerView;
    private FileRecyclerViewAdapter fileItemAdapter;
    private List<FileItem> fileItemArray;
    private ActivityResultLauncher<String> activityResultLauncher;
    private SharedPreferences pref;
    private Toolbar toolbar;
    private List<File> filesSelected;
    private FileOperations fileOperation;
    private URI currentDir;
    private RecyclerView storageDeviceRecyclerView;
    private StorageDeviceRecyclerViewAdapter storageDeviceAdapter;
    private ArrayList<StorageDeviceItem> storageDeviceItemArray;
    private AlertDialog currentDialog;
    private DirectoryContentsLoaderThread directoryContentsThread;
    private FileOperationThread fileOperationThread;
    private List<OnBackPressedCallback> backCallbacks=new ArrayList<>();

    public enum FileOperations{COPY,CUT,DELETE,NOOP};

    public enum SortModes{NAME,DATE,SIZE,NOOP};

    public enum DetailModes{SIZE,DATE};

    public class ToolbarMenuListener implements Toolbar.OnMenuItemClickListener
    {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlertDialog dialog;
            EditText filenameInput;
            TextView filenameText;
            View view;
            EditText dialogChangePathInput;
            switch(item.getItemId())
            {
                case R.id.toolbarClose:
                    finish();
                    break;
                case R.id.toolbarNewFile:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_create,null);
                    filenameInput=view.findViewById(R.id.dialogChangePathInput);
                    filenameText=view.findViewById(R.id.dialogChangePathText);
                    filenameText.setText("File name:");
                    dialog=new AlertDialog.Builder(FileManagerActivity.this).setTitle("Create new file:")
                            .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        FileUtils.touch(new File(currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(filenameInput.getText().toString()))));
                                        loadDirectoryContentsAndUpdateUI(currentDir);
                                    } catch (IOException e) {
                                        showErrorDialog(e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setView(view)
                            .create();
                    currentDialog=dialog;
                    dialog.show();
                    break;
                case R.id.toolbarNewFolder:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_create,null);
                    filenameInput=view.findViewById(R.id.dialogChangePathInput);
                    filenameText=view.findViewById(R.id.dialogChangePathText);
                    filenameText.setText("Folder name:");
                    dialog=new AlertDialog.Builder(FileManagerActivity.this).setTitle("Create new folder:")
                            .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        FileUtils.forceMkdir(new File(currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(filenameInput.getText().toString()))));
                                        loadDirectoryContentsAndUpdateUI(currentDir);
                                    } catch (IOException e) {
                                        showErrorDialog(e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setView(view)
                            .create();
                    currentDialog=dialog;
                    dialog.show();
                    break;
                case R.id.toolbarChangePath:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_change_path,null);
                    storageDeviceRecyclerView=view.findViewById(R.id.storageDeviceRecyclerView);
                    dialogChangePathInput=view.findViewById(R.id.dialogChangePathInput);
                    dialogChangePathInput.setText(currentDir.getPath());
                    populateStorageDevicesAvailable();
                    dialog=new AlertDialog.Builder(FileManagerActivity.this)
                            .setTitle("Change Current Path")
                            .setView(view)
                            .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        currentDir=new URI("file:"+UrlEscapers.urlPathSegmentEscaper().escape(dialogChangePathInput.getText().toString()));
                                        loadDirectoryContentsAndUpdateUI(currentDir);
                                        removeAllBackCallbacks();
                                    } catch (URISyntaxException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    currentDialog=dialog;
                    dialog.show();
                    break;
                case R.id.toolbarStorageDevices:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_storage_devices,null);
                    storageDeviceRecyclerView=view.findViewById(R.id.storageDeviceRecyclerView);
                    populateStorageDevicesAvailable();
                    dialog=new AlertDialog.Builder(FileManagerActivity.this)
                            .setTitle("Storage Devices")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setView(view)
                            .create()
                            ;
                    currentDialog=dialog;
                    dialog.show();
                    break;
                case  R.id.toolbarPaste:
                    performFileOperation();
                    AppState.instance().setSomethingInClipboard(false);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                toolbar.getMenu().findItem(R.id.toolbarPaste).setVisible(false);
                            });
                        }
                    },300);
                    break;
                case R.id.toolbarSettings:
                    Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    }

    public class ToolbarSelectionMenuListener implements Toolbar.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlertDialog dialog;
            EditText filenameInput;
            TextView filenameText;
            View view;
            EditText dialogChangePathInput;
            switch(item.getItemId())
            {
                case R.id.toolbarSelectAll:
                    fileItemAdapter.checkEveryone();
                    break;
                case R.id.toolbarCopy2:
                    memorizeFilesSelected();
                    fileOperation=FileOperations.COPY;
                    AppState.instance().setSomethingInClipboard(true);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() ->{
                                onBackPressed();
                            });
                        }
                    },300);
                    break;
                case R.id.toolbarCut2:
                    memorizeFilesSelected();
                    fileOperation=FileOperations.CUT;
                    AppState.instance().setSomethingInClipboard(true);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() ->{
                                onBackPressed();
                            });
                        }
                    },300);
                    break;
                case R.id.toolbarRename:
                    memorizeFilesSelected();
                    File selected=filesSelected.get(0);
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_rename,null);
                    filenameInput=view.findViewById(R.id.dialogChangePathInput);
                    filenameText=view.findViewById(R.id.dialogChangePathText);
                    filenameInput.setText(selected.getName());
                    dialog=new AlertDialog.Builder(FileManagerActivity.this).setTitle("Rename item:")
                            .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        URI renamed=currentDir.resolve(UrlEscapers.urlPathSegmentEscaper().escape(filenameInput.getText().toString()));
                                        if(!selected.toURI().equals(renamed))
                                        {
                                            Files.move(selected, new File(renamed));
                                        }
                                        onBackPressed();
                                        loadDirectoryContentsAndUpdateUI(currentDir);
                                    } catch (IOException e) {
                                        showErrorDialog(e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setView(view)
                            .create();
                    if(selected.isDirectory())
                    {
                        filenameText.setText("Change name for folder "+selected.getName()+":");
                        dialog.setTitle("Rename folder:");
                    }
                    else
                    {
                        filenameText.setText("Change name for file "+selected.getName()+":");
                        dialog.setTitle("Rename file:");
                    }
                    currentDialog=dialog;
                    dialog.show();
                    break;
                case R.id.toolbarDelete2:
                    memorizeFilesSelected();
                    String items;
                    if(filesSelected.size()==1)
                    {
                        items="item";
                    }
                    else
                    {
                        items="items";
                    }
                    dialog=new AlertDialog.Builder(FileManagerActivity.this)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to delete "+filesSelected.size()+" "+items+"?")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fileOperation=FileOperations.DELETE;
                                    onBackPressed();
                                    performFileOperation();
                                    fileOperation=FileOperations.NOOP;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    dialog.show();
                    break;
                case R.id.toolbarSettings2:
                    Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        AppState.setContext(getApplicationContext());
        AppState.instance().setFileManagerActivity(this);
        pref=AppState.instance().getPreferences();
        if(pref.getBoolean("systemNightModeChecked",true))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if(pref.getBoolean("nightModeChecked",false))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        AppState.instance().setSortMode(AppState.sortModeTranslated(pref.getString("sortMode","NAME")));
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted ->{
            if(granted)
            {
                //good!! :))
            }
            else
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        toolbar=findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new ToolbarMenuListener());
        fileRecyclerView=findViewById(R.id.fileRecyclerView);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileRecyclerView.setNestedScrollingEnabled(false);
        currentDir=AppState.instance().getCurrentDir();
        if(currentDir==null)
        {
            String firstStorageDevicePath=getExternalFilesDir(null).toURI().getPath().split("Android")[0];
            currentDir=URI.create("file:"+firstStorageDevicePath);
        }
        toolbar.setSubtitle(currentDir.getPath());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWritePermission();
    }

    @Override
    public void onBackPressed() {
        if(getOnBackPressedDispatcher().hasEnabledCallbacks())
        {
            super.onBackPressed();
        }
        else
        {
            File currentDirFile = new File(currentDir);
            File parentFile = currentDirFile.getParentFile();
            if (parentFile != null)
            {
                loadDirectoryContentsAndUpdateUI(parentFile.toURI());
            }
            else
            {
                finish();
            }
        }
    }

    private List<FileItem> loadDirectoryContents(URI uri)
    {
        currentDir=uri;
        AppState.instance().setCurrentDir(currentDir);
        List<FileItem> fileItemsArray=new CopyOnWriteArrayList<>();
        if(directoryContentsThread!=null)
        {
            while(directoryContentsThread.getState()!=Thread.State.TERMINATED);
        }
        directoryContentsThread=new DirectoryContentsLoaderThread(uri,fileItemsArray);
        directoryContentsThread.shouldNotUpdateUI();
        directoryContentsThread.start();
        return fileItemsArray;
    }

    private List<FileItem> loadDirectoryContents(URI uri,boolean updateUI)
    {
        currentDir=uri;
        AppState.instance().setCurrentDir(currentDir);
        List<FileItem> fileItemsArray=new CopyOnWriteArrayList<>();
        if(directoryContentsThread!=null)
        {
            while(directoryContentsThread.getState()!=Thread.State.TERMINATED);
        }
        directoryContentsThread=new DirectoryContentsLoaderThread(uri,fileItemsArray);
        if(updateUI)
        {
            directoryContentsThread.shouldUpdateUI();
        }
        else
        {
            directoryContentsThread.shouldNotUpdateUI();
        }
        directoryContentsThread.setSortMode(AppState.instance().getSortMode());
        directoryContentsThread.setDetailMode(DetailModes.DATE);
        directoryContentsThread.start();
        return fileItemsArray;
    }

    private void checkWritePermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setMessage("File managing permission has not been granted!\nThe permission is required for the app to be able to manage files on the device!")
                    .setTitle("Permission required")
                    .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    });
            builder.create().show();
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            loadDirectoryContentsAndUpdateUI(currentDir);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void loadDirectoryContentsAndUpdateUI(URI uri)
    {
        fileItemArray=loadDirectoryContents(uri,true);
    }

    public void updateDirectoryContentsUI()
    {
        toolbar.setSubtitle(currentDir.getPath());
        fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
    }

    private void memorizeFilesSelected()
    {
        filesSelected=new CopyOnWriteArrayList<>();
        for(FileItem fi:fileItemArray)
        {
            if(fi.isChecked())
            {
                filesSelected.add(new File(fi.getUri()));
            }
        }
    }

    private void performFileOperation()
    {
        if(fileOperationThread!=null)
        {
            while(fileOperationThread.getState()!=Thread.State.TERMINATED);
        }
        fileOperationThread=new FileOperationThread(filesSelected);
        fileOperationThread.setFileOperation(fileOperation);
        fileOperationThread.start();
    }

    private void populateStorageDevicesAvailable()
    {
        storageDeviceItemArray=findStorageDevicesAvailable();
        storageDeviceAdapter=new StorageDeviceRecyclerViewAdapter(storageDeviceItemArray,this);
        storageDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        storageDeviceRecyclerView.setAdapter(storageDeviceAdapter);
    }

    private ArrayList<StorageDeviceItem> findStorageDevicesAvailable()
    {
        ArrayList<StorageDeviceItem> sdiarray=new ArrayList<>();
        StorageDeviceItem sdi;
        File[] devices=getExternalFilesDirs(null);
        int i=0;
        for(File d:devices)
        {
            sdi=createStorageDeviceItem(d,i);
            i++;
            sdiarray.add(sdi);
        }
        return sdiarray;
    }

    private StorageDeviceItem createStorageDeviceItem(File d,int i)
    {
        StorageDeviceItem sdi=new StorageDeviceItem();
        try {
            sdi.setMountPath(new URI(d.toURI().toString().split("Android")[0]));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if(i==0)
        {
            sdi.setIcon(AppCompatResources.getDrawable(this,R.drawable.cellphone));
            sdi.setTitle("Internal Storage");
        }
        else
        {
            sdi.setIcon(AppCompatResources.getDrawable(this,R.drawable.sd));
            sdi.setTitle("External Storage");
        }
        updateStorageDeviceItemFileSystemStatus(sdi);
        return sdi;
    }

    public AlertDialog getCurrentDialog() {
        return currentDialog;
    }

    public static void updateStorageDeviceItemFileSystemStatus(StorageDeviceItem sdi)
    {
        StatFs statFs=new StatFs(sdi.getMountPath().getPath());
        sdi.setFreeBytes(statFs.getAvailableBytes());
        sdi.setTotalBytes(statFs.getTotalBytes());
        sdi.setUsedBytes(sdi.getTotalBytes()-sdi.getFreeBytes());
        sdi.setFreeGB("free\n"+AppState.filesizeDisplayString(sdi.getFreeBytes()));
        sdi.setTotalGB("total\n"+AppState.filesizeDisplayString(sdi.getTotalBytes()));
        sdi.setUsedGB("used\n"+AppState.filesizeDisplayString(sdi.getUsedBytes()));
        sdi.setPercentageUsed((int)((double)sdi.getUsedBytes()/(double)sdi.getTotalBytes()*10000));
    }

    public void addBackCallback(OnBackPressedCallback callback)
    {
        backCallbacks.add(callback);
        getOnBackPressedDispatcher().addCallback(callback);
    }

    public void removeAllBackCallbacks()
    {
        for(OnBackPressedCallback b:backCallbacks)
        {
            if(b!=null)
            {
                b.remove();
            }
        }
        backCallbacks=new ArrayList<>();
    }

    public void showErrorDialog(String errorMessage)
    {
        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();
    }

}