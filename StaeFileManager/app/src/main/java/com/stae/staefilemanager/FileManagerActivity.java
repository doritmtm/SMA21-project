package com.stae.staefilemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

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
}