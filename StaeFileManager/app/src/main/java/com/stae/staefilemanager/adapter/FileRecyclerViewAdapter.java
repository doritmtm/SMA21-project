package com.stae.staefilemanager.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stae.staefilemanager.FileManagerActivity;
import com.stae.staefilemanager.R;
import com.stae.staefilemanager.model.FileItem;

import java.io.File;
import java.util.ArrayList;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {
    private ArrayList<FileItem> fileItemArray;
    private FileManagerActivity fileManagerActivity;

    public FileRecyclerViewAdapter(ArrayList<FileItem> fileItemArray, FileManagerActivity fileManagerActivity) {
        this.fileItemArray = fileItemArray;
        this.fileManagerActivity = fileManagerActivity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView fileNameText;
        private ImageView iconView;
        private FileItem fileItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText=itemView.findViewById(R.id.fileNameText);
            iconView=itemView.findViewById(R.id.iconView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(new File(fileItem.getUri()).isDirectory())
                    {
                        fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getUri());
                        fileManagerActivity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                if(fileItem.getParentURI()!=null)
                                {
                                    fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getParentURI());
                                    remove();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem=fileItemArray.get(position);
        holder.fileNameText.setText(fileItem.getName());
        holder.iconView.setImageDrawable(fileItem.getIcon());
        holder.fileItem=fileItem;
    }

    @Override
    public int getItemCount() {
        return fileItemArray.size();
    }



}
