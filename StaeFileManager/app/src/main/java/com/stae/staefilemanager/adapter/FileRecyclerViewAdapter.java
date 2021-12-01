package com.stae.staefilemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stae.staefilemanager.R;
import com.stae.staefilemanager.model.FileItem;

import java.util.ArrayList;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.ViewHolder> {
    private ArrayList<FileItem> fileItemArray;

    public FileRecyclerViewAdapter(ArrayList<FileItem> fileItemArray) {
        this.fileItemArray = fileItemArray;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView fileNameText;
        private ImageView iconView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText=itemView.findViewById(R.id.fileNameText);
            iconView=itemView.findViewById(R.id.iconView);
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
    }

    @Override
    public int getItemCount() {
        return fileItemArray.size();
    }



}
