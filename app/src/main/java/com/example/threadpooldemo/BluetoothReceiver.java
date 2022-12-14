package com.example.threadpooldemo;


import static android.bluetooth.BluetoothClass.Device.Major.IMAGING;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver {
    private BluetoothListener listener;

    public BluetoothReceiver(BluetoothListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == BluetoothAdapter.ACTION_DISCOVERY_STARTED) {
            listener.onDiscoveryStart();
        } else if (intent.getAction() == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
            listener.onDiscoveryFinish();
        } else if (intent.getAction() == BluetoothDevice.ACTION_FOUND) {
            BluetoothDevice blueDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (blueDevice != null) {
                Log.i("hprt","onReceive: "+blueDevice.getName()+"   "+blueDevice.getBluetoothClass().getMajorDeviceClass()+"   "+blueDevice.getAddress());
                if (blueDevice.getBluetoothClass().getMajorDeviceClass() == IMAGING) {
                    listener.onDeviceFound(blueDevice);
                }
            }
        } else if (intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            listener.onDeviceBondStateChange(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1));
        } else if (intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            listener.onDeviceLost(device);
        } else if (intent.getAction() == BluetoothAdapter.ACTION_STATE_CHANGED) {
            listener.onBluetoothStateChange(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
        }
    }

    interface BluetoothListener {
        /**
         * ?????????????????????????????????
         */
        void onDiscoveryStart();

        /**
         * ??????????????????????????????
         *
         * @param device ???????????????????????????
         */
        void onDeviceFound(BluetoothDevice device);

        /**
         * ?????????????????????????????????????????????
         *
         * @param state ?????????????????????
         */
        void onDeviceBondStateChange(int state);

        /**
         * ????????????????????????
         */
        void onDeviceLost(BluetoothDevice device);

        /**
         * ??????????????????????????????
         *
         * @param state ??????????????????
         */
        void onBluetoothStateChange(int state);

        /**
         * ????????????????????????
         */
        void onDiscoveryFinish();
    }
}
