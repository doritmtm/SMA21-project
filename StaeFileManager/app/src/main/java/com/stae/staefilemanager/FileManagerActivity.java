package com.stae.staefilemanager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.io.Files;
import com.stae.staefilemanager.adapter.FileRecyclerViewAdapter;
import com.stae.staefilemanager.model.FileItem;
import com.stae.staefilemanager.ui.CustomRecyclerView;
import com.stae.staefilemanager.ui.LockableNestedScrollView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class FileManagerActivity extends AppCompatActivity {
    private CustomRecyclerView fileRecyclerView;
    private ArrayList<FileItem> fileItemArray;
    private ActivityResultLauncher<String> activityResultLauncher;
    private LockableNestedScrollView fileScroll;
    private SharedPreferences pref;
    private Toolbar toolbar;
    private LinearLayout linear1;
    private ArrayList<File> filesSelected;
    private FileOperations fileOperation;
    private enum FileOperations{COPY,CUT};
    private URI currentDir;

    public class ToolbarMenuListener implements Toolbar.OnMenuItemClickListener
    {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AlertDialog dialog;
            EditText filenameInput;
            TextView filenameText;
            View view;
            File currentDirFile=new File(currentDir);
            switch(item.getItemId())
            {
                case R.id.toolbarNewFile:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_create,null);
                    filenameInput=view.findViewById(R.id.dialogFilenameInput);
                    filenameText=view.findViewById(R.id.dialogFilenameText);
                    filenameText.setText("File name:");
                    dialog=new AlertDialog.Builder(FileManagerActivity.this).setTitle("Create new file:")
                            .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("MYAPPPP",currentDir.resolve(filenameInput.getText().toString()).toString());
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
                    dialog.show();
                    break;
                case R.id.toolbarNewFolder:
                    view=LayoutInflater.from(FileManagerActivity.this).inflate(R.layout.dialog_create,null);
                    filenameInput=view.findViewById(R.id.dialogFilenameInput);
                    filenameText=view.findViewById(R.id.dialogFilenameText);
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
        linear1=findViewById(R.id.linear1);
        fileScroll=findViewById(R.id.fileScroll);
        fileScroll.post(() -> fileScroll.scrollTo(0,0));
        fileRecyclerView.setNestedScrollView(fileScroll);
        fileItemArray=loadDirectoryContents(URI.create("file:/sdcard/"));
        FileRecyclerViewAdapter fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWritePermission();
    }

    private ArrayList<FileItem> loadDirectoryContents(URI uri)
    {
        currentDir=uri;
        File file=new File(uri);
        ArrayList<FileItem> fileItemsArray=new ArrayList<>();
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
        toolbar.setSubtitle(uri.getPath());
        fileItemArray=loadDirectoryContents(uri);
        FileRecyclerViewAdapter fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
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
        File currentDirFile=new File(currentDir);
        if(fileOperation==FileOperations.COPY)
        {
            for(File f:filesSelected)
            {
                try {
                    Files.copy(f,currentDirFile);
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
                    Files.move(f,currentDirFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}