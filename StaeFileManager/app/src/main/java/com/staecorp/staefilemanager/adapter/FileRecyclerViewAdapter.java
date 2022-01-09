package com.staecorp.staefilemanager.adapter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.Files;
import com.staecorp.staefilemanager.AppState;
import com.staecorp.staefilemanager.FileManagerActivity;
import com.staecorp.staefilemanager.R;
import com.staecorp.staefilemanager.model.FileItem;
import com.staecorp.staefilemanager.ui.CustomRecyclerView;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {
    private List<FileItem> fileItemArray;
    private FileManagerActivity fileManagerActivity;
    private static boolean selectionMode=false;
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
                    if (!selectionMode)
                    {
                        if (new File(fileItem.getUri()).isDirectory())
                        {
                            fileManagerActivity.loadDirectoryContentsAndUpdateUI(fileItem.getUri());
                        }
                        else
                        {
                            Intent intent=new Intent(Intent.ACTION_VIEW);
                            String mimetype= MimeTypeMap.getSingleton().getMimeTypeFromExtension(Files.getFileExtension(fileItem.getName()));
                            intent.setDataAndType(FileProvider.getUriForFile(AppState.getContext(),AppState.getContext().getPackageName()+".provider",new File(fileItem.getUri())),mimetype);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            try
                            {
                                AppState.getContext().startActivity(intent);
                            }
                            catch(ActivityNotFoundException e)
                            {
                                Toast.makeText(AppState.getContext(),"Cannot find an app to open this file",Toast.LENGTH_SHORT).show();
                            }
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
                    startSelectionMode();
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

    public void startSelectionMode()
    {
        if(!selectionMode) {
            selectionMode = true;
            nrSelected=0;
            toolbar.setTitle(nrSelected+" items selected");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    fileManagerActivity.runOnUiThread(() -> {
                        toolbar.getMenu().setGroupVisible(R.id.selectionGroup,true);
                        toolbar.getMenu().setGroupVisible(R.id.mainGroup,false);
                        toolbar.setOnMenuItemClickListener(fileManagerActivity.new ToolbarSelectionMenuListener());
                    });
                }
            },300);
            fileManagerActivity.addBackCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    stopSelectionMode();
                    remove();
                }
            });
        }
    }

    public void stopSelectionMode()
    {
        uncheckEveryone();
        fileRecyclerView.setLocked(false);
        fileRecyclerView.setSelectionMode(false);
        selectionMode = false;
        toolbar.setTitle(R.string.app_name);
        toolbar.getMenu().setGroupVisible(R.id.selectionGroup,false);
        toolbar.getMenu().setGroupVisible(R.id.mainGroup,true);
        if(!AppState.instance().isSomethingInClipboard())
        {
            toolbar.getMenu().findItem(R.id.toolbarPaste).setVisible(false);
        }
        toolbar.setOnMenuItemClickListener(fileManagerActivity.new ToolbarMenuListener());
    }

}
