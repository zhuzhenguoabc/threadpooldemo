package com.example.threadpooldemo;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import androidx.recyclerview.widget.DividerItemDecoration;

public class DeviceListActivity extends AppCompatActivity {
    private Context mContext;
    private static final String TAG = "BluetoothActivity";

    private IntentFilter intentFilter;
    private BluetoothReceiver receiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothRVAdapter rvAdapter;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mContext = this;

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        rvAdapter = new BluetoothRVAdapter();
        rvAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Intent intent = new Intent();
                intent.putExtra("bluetooth", rvAdapter.getData().get(i));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setAdapter(rvAdapter);


        receiver = new BluetoothReceiver(new BluetoothReceiver.BluetoothListener() {
            @Override
            public void onDiscoveryStart() {
                Log.i(TAG, "onDiscoveryStart");
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                if (device.getName() != null && (device.getName().toLowerCase().contains("ft")
                                                || device.getName().toLowerCase().contains("dp")
                                                || device.getName().toLowerCase().contains("mt"))) {


                    for (BluetoothDevice element : rvAdapter.getData()) {
                        if (element.getName().equals(device.getName())) {
                            return;
                        }
                    }
                    rvAdapter.addData(device);
                    rvAdapter.notifyDataSetChanged();
                    Log.i(TAG, "onDeviceFound -> "+device.getName());
                }
            }

            @Override
            public void onDeviceBondStateChange(int state) {
                if (state == BluetoothDevice.BOND_NONE) {
                    Log.i(TAG, "配对失败");
                } else if (state == BluetoothDevice.BOND_BONDING) {
                    Log.i(TAG, "正在配对");
                } else if (state == BluetoothDevice.BOND_BONDED) {
                    Log.i(TAG, "配对成功");
                } else {
                    Log.i(TAG, "onDeviceBondStateChange -> UnKnow");
                }
            }

            @Override
            public void onDeviceLost(BluetoothDevice device) {
                Log.i(TAG, "onDeviceLost 设备丢失 -> "+device);
            }

            @Override
            public void onBluetoothStateChange(int state) {
                Log.i(TAG, "onBluetoothStateChange -> "+state);
                if (state == BluetoothAdapter.STATE_OFF) {

                } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {

                } else if (state == BluetoothAdapter.STATE_ON) {

                } else if (state == BluetoothAdapter.STATE_TURNING_ON) {

                } else {

                }
            }

            @Override
            public void onDiscoveryFinish() {
                Log.i(TAG, "onDiscoveryFinish");
            }
        });

        registerReceiver(receiver, intentFilter);
        bluetoothAdapter.enable();
        bluetoothAdapter.startDiscovery();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }
}