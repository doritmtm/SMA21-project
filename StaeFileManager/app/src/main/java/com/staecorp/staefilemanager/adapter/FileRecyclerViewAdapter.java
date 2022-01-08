package com.staecorp.staefilemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;
import com.staecorp.staefilemanager.R;
import com.staecorp.staefilemanager.model.FileItem;
import com.staecorp.staefilemanager.ui.CustomRecyclerView;

import java.io.File;
import java.util.List;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {
    private List<FileItem> fileItemArray;
    private FileManagerActivity fileManagerActivity;
    private boolean selectionMode=false;
    private CustomRecyclerView fileRecyclerView; //from FileManagerActivity
    private static int nrSelected=0;
    private Toolbar toolbar;

    public FileRecyclerViewAdapter(List<FileItem> fileItemArray, FileManagerActivity fileManagerActivity) {
        this.fileItemArray = fileItemArray;
        this.fileManagerActivity = fileManagerActivity;
        fileRecyclerView=fileManagerActivity.findViewById(R.id.fileRecyclerView);
        toolbar=fileManagerActivity.findViewById(R.id.toolbar);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView fileNameText,fileDetailsText;
        private ImageView iconView,checkView;
        private FileItem fileItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText=itemView.findViewById(R.id.fileNameText);
            fileDetailsText=itemView.findViewById(R.id.fileDetailsText);
            iconView=itemView.findViewById(R.id.iconView);
            checkView=itemView.findViewById(R.id.checkView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!selectionMode)
                    {
                        if (new File(fileItem.getUri()).isDirectory()) {
                            fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getUri());
                            /*fileManagerActivity.addBackCallback(new OnBackPressedCallback(true) {
                                @Override
                                public void handleOnBackPressed() {
                                    if (fileItem.getParentURI() != null) {
                                        fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getParentURI());
                                        remove();
                                    }
                                }
                            });*/
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
                        //changeSelection();
                        toolbar.setTitle(nrSelected+" items selected");
                        //toolbar.getMenu().clear();
                        //toolbar.inflateMenu(R.menu.toolbar_selection_menu);
                        toolbar.getMenu().setGroupVisible(R.id.selectionGroup,true);
                        toolbar.getMenu().setGroupVisible(R.id.mainGroup,false);
                        toolbar.setOnMenuItemClickListener(fileManagerActivity.new ToolbarSelectionMenuListener());
                        fileManagerActivity.addBackCallback(new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                stopSelectionMode();
                                remove();
                            }
                        });
                    }
                    changeSelection();
                    fileRecyclerView.setLocked(true);
                    fileRecyclerView.setSelectionMode(true);
                    fileRecyclerView.setCurrentChildInFocus(itemView);
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
        holder.fileDetailsText.setText(fileItem.getDetail());
        holder.iconView.setImageDrawable(fileItem.getIcon());
        holder.fileItem=fileItem;
        if(fileItem.isChecked())
        {
            holder.checkView.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.checkView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fileItemArray.size();
    }

    public void checkEveryone()
    {
        nrSelected=0;
        for(FileItem fi:fileItemArray)
        {
            fi.setChecked(true);
            if(fi.getViewHolder()!=null)
            {
                fi.getViewHolder().checkView.setVisibility(View.VISIBLE);
            }
            nrSelected++;
        }
        notifyDataSetChanged();
        toolbar.setTitle(nrSelected+" items selected");
    }

    public void uncheckEveryone()
    {
        for(FileItem fi:fileItemArray)
        {
            fi.setChecked(false);
            if(fi.getViewHolder()!=null)
            {
                fi.getViewHolder().checkView.setVisibility(View.INVISIBLE);
            }
        }
        notifyDataSetChanged();
        nrSelected=0;
        toolbar.setTitle(nrSelected+" items selected");
    }

    public void stopSelectionMode()
    {
        uncheckEveryone();
        fileRecyclerView.setLocked(false);
        fileRecyclerView.setSelectionMode(false);
        selectionMode = false;
        toolbar.setTitle(R.string.app_name);
        //toolbar.getMenu().clear();
        toolbar.getMenu().setGroupVisible(R.id.selectionGroup,false);
        toolbar.getMenu().setGroupVisible(R.id.mainGroup,true);
        if(!AppState.instance().isSomethingInClipboard())
        {
            toolbar.getMenu().findItem(R.id.toolbarPaste).setVisible(false);
        }
        //toolbar.getMenu().removeGroup();
        //toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(fileManagerActivity.new ToolbarMenuListener());
    }

}
