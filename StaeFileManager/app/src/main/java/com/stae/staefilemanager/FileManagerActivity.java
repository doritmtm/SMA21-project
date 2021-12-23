package com.stae.staefilemanager;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.io.Files;
import com.stae.staefilemanager.adapter.FileRecyclerViewAdapter;
import com.stae.staefilemanager.adapter.StorageDeviceRecyclerViewAdapter;
import com.stae.staefilemanager.model.FileItem;
import com.stae.staefilemanager.model.StorageDeviceItem;
import com.stae.staefilemanager.thread.DirectoryContentsLoaderThread;
import com.stae.staefilemanager.ui.CustomRecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileManagerActivity extends AppCompatActivity {
    private CustomRecyclerView fileRecyclerView;
    private FileRecyclerViewAdapter fileItemAdapter;
    private List<FileItem> fileItemArray;
    private ActivityResultLauncher<String> activityResultLauncher;
    private SharedPreferences pref;
    private Toolbar toolbar;
    private ArrayList<File> filesSelected;
    private FileOperations fileOperation;
    private enum FileOperations{COPY,CUT,DELETE,NOOP};
    private URI currentDir;
    private RecyclerView storageDeviceRecyclerView;
    private StorageDeviceRecyclerViewAdapter storageDeviceAdapter;
    private ArrayList<StorageDeviceItem> storageDeviceItemArray;
    private AlertDialog currentDialog;
    private DirectoryContentsLoaderThread directoryContentsThread;

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
                                        Files.touch(new File(currentDir.resolve(filenameInput.getText().toString())));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    loadDirectoryContentsAndUpdateUI(currentDir);
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
                                        if(!new File(currentDir.resolve(filenameInput.getText().toString())).mkdir())
                                        {
                                            throw new IOException();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    loadDirectoryContentsAndUpdateUI(currentDir);
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
                                        currentDir=new URI("file:"+dialogChangePathInput.getText().toString());
                                        loadDirectoryContentsAndUpdateUI(currentDir);
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
            switch(item.getItemId())
            {
                case R.id.toolbarCopy2:
                    memorizeFilesSelected();
                    fileOperation=FileOperations.COPY;
                    break;
                case R.id.toolbarCut2:
                    memorizeFilesSelected();
                    fileOperation=FileOperations.CUT;
                    break;
                case R.id.toolbarDelete2:
                    memorizeFilesSelected();
                    fileOperation=FileOperations.DELETE;
                    performFileOperation();
                    fileOperation=FileOperations.NOOP;
                    onBackPressed();
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
        setSupportActionBar(toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new ToolbarMenuListener());
        fileRecyclerView=findViewById(R.id.fileRecyclerView);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileRecyclerView.setNestedScrollingEnabled(false);
        loadDirectoryContentsAndUpdateUI(URI.create(getExternalFilesDir(null).toURI().toString().split("Android")[0]));
        fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWritePermission();
    }

    private List<FileItem> loadDirectoryContents(URI uri)
    {
        currentDir=uri;
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
        List<FileItem> fileItemsProgArray=new ArrayList<>();
        FileRecyclerViewAdapter fileItemAdapter=new FileRecyclerViewAdapter(fileItemsProgArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
        int i=0;
        for(FileItem fi:fileItemArray)
        {
            fileItemsProgArray.add(fi);
            fileItemAdapter.notifyItemInserted(i);
            i++;
        }
    }

    private void memorizeFilesSelected()
    {
        filesSelected=new ArrayList<>();
        for(FileItem fi:fileItemArray)
        {
            if(fi.isChecked())
            {
                filesSelected.add(new File(fi.getUri()));
            }
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
            fileItem.setIcon(AppCompatResources.getDrawable(this,R.drawable.folder));
        }
        else
        {
            fileItem.setIcon(AppCompatResources.getDrawable(this,R.drawable.file));
        }
        return fileItem;
    }

    private void performFileOperation()
    {
        if(fileOperation==FileOperations.COPY)
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
        if(fileOperation==FileOperations.CUT)
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
        if(fileOperation==FileOperations.DELETE)
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
        fileOperation=FileOperations.NOOP;
        loadDirectoryContentsAndUpdateUI(currentDir);
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
        sdi.setFreeGB("free\n"+String.format("%,.2f",(double)sdi.getFreeBytes()/1073741824.0)+" GB");
        sdi.setTotalGB("total\n"+String.format("%,.2f",(double)sdi.getTotalBytes()/1073741824.0)+" GB");
        sdi.setUsedGB("used\n"+String.format("%,.2f",(double)sdi.getUsedBytes()/1073741824.0)+" GB");
        sdi.setPercentageUsed((int)((double)sdi.getUsedBytes()/(double)sdi.getTotalBytes()*10000));
    }


}