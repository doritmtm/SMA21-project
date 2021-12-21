package com.stae.staefilemanager.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stae.staefilemanager.model.StorageDeviceItem;
import com.stae.staefilemanager.R;

import java.util.ArrayList;

public class StorageDeviceRecyclerViewAdapter extends RecyclerView.Adapter<StorageDeviceRecyclerViewAdapter.ViewHolder> {
    private ArrayList<StorageDeviceItem> storageDeviceItemArray;

    public StorageDeviceRecyclerViewAdapter(ArrayList<StorageDeviceItem> storageDeviceItemArray) {
        this.storageDeviceItemArray = storageDeviceItemArray;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView icon;
        private TextView title,path,usedSpace,freeSpace,totalSpace;
        private ProgressBar usageBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.storageDeviceIcon);
            title=itemView.findViewById(R.id.storageDeviceTitle);
            path=itemView.findViewById(R.id.storageDevicePath);
            usedSpace=itemView.findViewById(R.id.storageDeviceUsedSpace);
            freeSpace=itemView.findViewById(R.id.storageDeviceFreeSpace);
            totalSpace=itemView.findViewById(R.id.storageDeviceTotalSpace);
            usageBar=itemView.findViewById(R.id.storageDeviceProgressBar);
            usageBar.setMax(10000);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage_device,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageDeviceItem sdi=storageDeviceItemArray.get(position);
        holder.path.setText(sdi.getMountPath().toString().substring(5));
        holder.title.setText(sdi.getTitle());
        holder.icon.setImageDrawable(sdi.getIcon());
        holder.freeSpace.setText(sdi.getFreeGB());
        holder.totalSpace.setText(sdi.getTotalGB());
        holder.usedSpace.setText(sdi.getUsedGB());
        holder.usageBar.setProgress(sdi.getPercentageUsed());
        Log.d("MYAPPPP","percentage:"+sdi.getPercentageUsed());
    }

    @Override
    public int getItemCount() {
        return storageDeviceItemArray.size();
    }


}
