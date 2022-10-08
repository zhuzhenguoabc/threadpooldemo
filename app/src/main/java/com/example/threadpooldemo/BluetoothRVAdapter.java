package com.example.threadpooldemo;

import android.bluetooth.BluetoothDevice;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

public class BluetoothRVAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {

    public BluetoothRVAdapter() {
        super(R.layout.item_ble_device);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, BluetoothDevice device) {
        TextView tv_name = baseViewHolder.itemView.findViewById(R.id.tv_name);
        tv_name.setText(device.getName());

        TextView tv_mac = baseViewHolder.itemView.findViewById(R.id.tv_mac);
        tv_mac.setText(device.getAddress());
    }
}