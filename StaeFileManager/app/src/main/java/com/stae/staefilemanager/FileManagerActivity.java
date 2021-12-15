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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.stae.staefilemanager.adapter.FileRecyclerViewAdapter;
import com.stae.staefilemanager.model.FileItem;
import com.stae.staefilemanager.ui.CustomRecyclerView;
import com.stae.staefilemanager.ui.LockableNestedScrollView;

import java.io.File;
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

    public class ToolbarMenuListener implements Toolbar.OnMenuItemClickListener
    {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId())
            {
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
                    fileItem=new FileItem(f.getName());
                    fileItem.setUri(f.toURI());

                    if(f.getParentFile()!=null)
                    {
                        fileItem.setParentURI(f.getParentFile().toURI());
                    }
                    if(f.isDirectory())
                    {
                        fileItem.setIcon(AppCompatResources.getDrawable(this,R.drawable.folder));
                    }
                    else
                    {
                        fileItem.setIcon(AppCompatResources.getDrawable(this,R.drawable.file));
                    }
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.toolbarSettings:
                Intent intent=new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadDirectoryContentsAndUpdateUI(URI uri)
    {
        toolbar.setSubtitle(uri.getPath());
        fileItemArray=loadDirectoryContents(uri);
        FileRecyclerViewAdapter fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray,this);
        fileRecyclerView.setAdapter(fileItemAdapter);
    }

}