package com.stae.staefilemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.stae.staefilemanager.adapter.FileRecyclerViewAdapter;
import com.stae.staefilemanager.model.FileItem;

import java.util.ArrayList;

public class FileManagerActivity extends AppCompatActivity {
    private RecyclerView fileRecyclerView;
    private ArrayList<FileItem> fileItemArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        setSupportActionBar(findViewById(R.id.toolbar));
        fileItemArray=new ArrayList<>();
        fileItemArray.add(new FileItem("file01"));
        fileItemArray.add(new FileItem("FILE02"));
        fileRecyclerView=findViewById(R.id.fileRecyclerView);
        FileRecyclerViewAdapter fileItemAdapter=new FileRecyclerViewAdapter(fileItemArray);
        fileRecyclerView.setAdapter(fileItemAdapter);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
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
        return true;
    }
}