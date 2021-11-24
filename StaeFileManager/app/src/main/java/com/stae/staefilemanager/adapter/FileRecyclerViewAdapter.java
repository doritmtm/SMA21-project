package com.stae.staefilemanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText=itemView.findViewById(R.id.fileNameText);
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
        holder.fileNameText.setText(fileItemArray.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return fileItemArray.size();
    }



}
