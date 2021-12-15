package com.stae.staefilemanager.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.stae.staefilemanager.FileManagerActivity;
import com.stae.staefilemanager.R;
import com.stae.staefilemanager.model.FileItem;
import com.stae.staefilemanager.ui.CustomRecyclerView;
import com.stae.staefilemanager.ui.LockableNestedScrollView;

import java.io.File;
import java.util.ArrayList;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {
    private ArrayList<FileItem> fileItemArray;
    private FileManagerActivity fileManagerActivity;
    private boolean selectionMode=false;
    private LockableNestedScrollView fileScroll; //from FileManagerActivity
    private CustomRecyclerView fileRecyclerView; //from FileManagerActivity
    private int nrSelected=0;
    private Toolbar toolbar;

    public FileRecyclerViewAdapter(ArrayList<FileItem> fileItemArray, FileManagerActivity fileManagerActivity) {
        this.fileItemArray = fileItemArray;
        this.fileManagerActivity = fileManagerActivity;
        fileScroll=fileManagerActivity.findViewById(R.id.fileScroll);
        fileRecyclerView=fileManagerActivity.findViewById(R.id.fileRecyclerView);
        toolbar=fileManagerActivity.findViewById(R.id.toolbar);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView fileNameText;
        private ImageView iconView,checkView;
        private FileItem fileItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText=itemView.findViewById(R.id.fileNameText);
            iconView=itemView.findViewById(R.id.iconView);
            checkView=itemView.findViewById(R.id.checkView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!selectionMode)
                    {
                        if (new File(fileItem.getUri()).isDirectory()) {
                            fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getUri());
                            fileManagerActivity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                                @Override
                                public void handleOnBackPressed() {
                                    if (fileItem.getParentURI() != null) {
                                        fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getParentURI());
                                        remove();
                                    }
                                }
                            });
                        }
                    }
                    else
                    {
                        changeSelection();
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(!selectionMode) {
                        selectionMode = true;
                        nrSelected=0;
                        changeSelection();
                        toolbar.setTitle(nrSelected+" items selected");
                        fileScroll.setLocked(true);
                        fileRecyclerView.setSelectionMode(true);
                        fileRecyclerView.setCurrentChildInFocus(itemView);
                        fileManagerActivity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                uncheckEveryone();
                                fileScroll.setLocked(false);
                                fileRecyclerView.setSelectionMode(false);
                                selectionMode = false;
                                toolbar.setTitle(R.string.app_name);
                                remove();
                            }
                        });
                    }
                    return true;
                }
            });
        }

        public void changeSelection()
        {
            if(fileItem.isChecked())
            {
                fileItem.setChecked(false);
                checkView.setVisibility(View.INVISIBLE);
                nrSelected--;
            }
            else
            {
                fileItem.setChecked(true);
                checkView.setVisibility(View.VISIBLE);
                nrSelected++;
            }
            toolbar.setTitle(nrSelected+" items selected");
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
        fileItem.setViewHolder(holder);
        holder.fileNameText.setText(fileItem.getName());
        holder.iconView.setImageDrawable(fileItem.getIcon());
        holder.fileItem=fileItem;
    }

    @Override
    public int getItemCount() {
        return fileItemArray.size();
    }

    private void checkEveryone()
    {
        for(FileItem fi:fileItemArray)
        {
            fi.getViewHolder().fileItem.setChecked(true);
            fi.getViewHolder().checkView.setVisibility(View.VISIBLE);
        }
    }

    private void uncheckEveryone()
    {
        for(FileItem fi:fileItemArray)
        {
            fi.getViewHolder().fileItem.setChecked(false);
            fi.getViewHolder().checkView.setVisibility(View.INVISIBLE);
        }
        nrSelected=0;
        toolbar.setTitle(nrSelected+" items selected");
    }

}
