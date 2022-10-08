package com.example.threadpooldemo;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.threadpooldemo.Utils.FileUtil;
import com.example.threadpooldemo.Utils.ThreadExecutors;
import com.hprt.lib_ft800.FTHelper;
import com.hprt.lib_ft800.utils.*;
import com.hprt.lib_ft800.listener.DoResultListener;
import com.hprt.lib_ft800.listener.ModelListener;
import com.hprt.lib_ft800.listener.NameListener;
import com.hprt.lib_ft800.listener.SnListener;
import com.hprt.lib_ft800.listener.WiFiListener;
import com.hprt.lib_ft800.listener.WiFiVersionListener;
import com.hprt.lib_ft800.listener.*;

import java.io.File;
import java.util.ArrayList;


public class ConfigNetActivity extends AppCompatActivity {
    private Context mContext;

    private RecyclerView recyclerView;
    private TextView tv_msg;

    private BluetoothDevice bluetoothDevice;
    private BaseQuickAdapter adapter;
    private ArrayList<String> cmds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_network);

        mContext = this;

        recyclerView = findViewById(R.id.recyclerview);
        tv_msg = findViewById(R.id.tv_msg);

        addItems();
        adapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_main, cmds) {
            @Override
            protected void convert(@NonNull BaseViewHolder helper, String item) {
                helper.setText(R.id.text1, item);
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayout.VERTICAL));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (cmds.get(i).equals(mContext.getResources().getString(R.string.scan))) {
                    startActivityForResult(new Intent(mContext, DeviceListActivity.class),1001);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.connect))) {
                    connect();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.disconnect))) {
                    disconnect();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.config_net))) {
                    configNet();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.wifi_info))) {
                    getWiFiInfo();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.set_wifi_mode))) {
                    setWiFiMode();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.name))) {
                    getName();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.sn))) {
                    getSn();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.model))) {
                    getModel();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.wifi_version))) {
                    getWiFiVersion();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.version))) {
                    getPrinterVersion();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.cut_study))) {
                    FTHelper.INSTANCE.cutStudyByBluetooth(new DoResultListener() {
                        @Override
                        public void onResult(int code) {
                            if (code == 0) {
                                tv_msg.setText("切刀学习成功");
                            } else {
                                tv_msg.setText("切刀学习失败");
                            }
                        }
                    });
                } /*else if (cmds.get(i).equals("test") {
                    test();
                }*/
            }
        });
    }


    private void addItems(){
        cmds.add(mContext.getResources().getString(R.string.scan));
        cmds.add(mContext.getResources().getString(R.string.connect));
        cmds.add(mContext.getResources().getString(R.string.disconnect));
        cmds.add(mContext.getResources().getString(R.string.config_net));
        cmds.add(mContext.getResources().getString(R.string.wifi_info));
        cmds.add(mContext.getResources().getString(R.string.set_wifi_mode));
        cmds.add(mContext.getResources().getString(R.string.name));
        cmds.add(mContext.getResources().getString(R.string.sn));
        cmds.add(mContext.getResources().getString(R.string.model));
        cmds.add(mContext.getResources().getString(R.string.wifi_version));
        cmds.add(mContext.getResources().getString(R.string.version));
        cmds.add(mContext.getResources().getString(R.string.cut_study));
//        cmds.add("打印自检页");
//        cmds.add("test");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 1001) {
                bluetoothDevice = data.getParcelableExtra("bluetooth");
                tv_msg.setText("设备："+ bluetoothDevice.getName() + "mac=" + bluetoothDevice.getAddress());
            }
        }
    }

    @Override
    protected void onDestroy() {
        FTHelper.INSTANCE.disconnectBluetooth();
        super.onDestroy();
    }

    private ArrayList<String> getSecurityList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("OPEN");
        list.add("WEP");
        list.add("WPA2-PSK");
        list.add("WPA/WPA2-PSK");
        list.add("WPA-PSK");
        list.add("WPA");
        list.add("WPA2");
        list.add("SAE");
        list.add("UNKNOWN");
        return list;
    }

    private void connect() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (bluetoothDevice == null) {
                    ThreadExecutors.mainThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            tv_msg.append("\n 请先选择蓝牙设备");
                        }
                    });
                }
                boolean con = FTHelper.INSTANCE.connectBluetooth(bluetoothDevice.getAddress());

                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(con){
                            tv_msg.append("\n 连接成功");
                        }else{
                            tv_msg.append("\n 连接失败");
                        }
                    }
                });
            }
        }.start();
    }

    private void disconnect() {
        FTHelper.INSTANCE.disconnectBluetooth();
        tv_msg.setText("连接断开");
    }

    private void configNet() {
        MaterialDialog dialog = new MaterialDialog.Builder(ConfigNetActivity.this)
                .title("请输入网络参数")
                .customView(R.layout.dialog_config_network, false)
                .build();
        dialog.show();

        EditText et_ssid = (EditText) dialog.findViewById(R.id.et_ssid);
        EditText et_pass = (EditText) dialog.findViewById(R.id.et_pass);
        Button btn_security = (Button) dialog.findViewById(R.id.btn_security);

        btn_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(ConfigNetActivity.this)
                        .title("请选择加密类型")
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                btn_security.setText(text);
                                btn_security.setTag(position);
                            }
                        }).build();
                dialog.show();
            }
        });


        dialog.findViewById(R.id.btn_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            boolean con =  FTHelper.INSTANCE.configNet(
                                    et_ssid.getText().toString(),
                                    et_pass.getText().toString(),
                                    (int)btn_security.getTag());
                            LogUtils.d("configNet == "+con);
                            byte[] readResult = FTHelper.INSTANCE.readBluetooth(30*1000);

                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //tv_msg.setText(ByteUtils.bytetohex(readResult));
                                }
                            });
                        }catch (Exception e) {
                            e.printStackTrace();
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort("配网写入失败");
                                }
                            });
                        }
                    }
                }.start();

                dialog.dismiss();
            }
        });
    }


    private void getWiFiInfo() {
        FTHelper.INSTANCE.getWiFiInfoBluetooth(new WiFiListener() {
            @Override
            public void onMode(int mode) {

            }

            @Override
            public void onSSID(String ssid) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.append("ssid ="+ssid+"\n");
                    }
                });
            }

            @Override
            public void onIpAddress(String ip) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.append("ip ="+ip+"\n");
                    }
                });
            }
        });
    }

    private void setWiFiMode() {
        String[] list = new String[]{"AP","STA"};
        MaterialDialog dialog = new MaterialDialog.Builder(ConfigNetActivity.this)
                .items(list)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        dialog.dismiss();

                        boolean result = FTHelper.INSTANCE.setWiFiModeBluetooth(position+1);

                        ThreadExecutors.mainThread.execute(new Runnable() {
                            @Override
                            public void run() {
                                if(result){
                                    ToastUtils.showShort("设置成功");
                                } else{
                                    ToastUtils.showShort("设置失败");
                                }
                            }
                        });
                    }
                }).build();
        dialog.show();
    }

    private void getName() {
        FTHelper.INSTANCE.getNameByBluetooth(new NameListener() {
            @Override
            public void onName(String name) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("name ="+name);
                    }
                });
            }
        });
    }

    private void getSn() {
        FTHelper.INSTANCE.getSnByBluetooth(new SnListener() {
            @Override
            public void getSn(String sn) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("sn ="+sn);
                    }
                });
            }
        });
    }

    private void getModel() {
        FTHelper.INSTANCE.getModelByBluetooth(new ModelListener() {
            @Override
            public void onModel(String model) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("model ="+model);
                    }
                });
            }
        });
    }

    private void getWiFiVersion() {
        FTHelper.INSTANCE.getWifiVersionByBluetooth(new WiFiVersionListener() {
            @Override
            public void onVersion(String version) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("version ="+version);
                    }
                });
            }
        });
    }

    private void getPrinterVersion() {
        /*FTHelper.INSTANCE.getFirmwareVersionByBluetooth(new FirmwareListener() {
            @Override
            public void onVersion(String version) {
                ThreadExecutors.mainThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("printer version ="+version);
                    }
                });
            }
        });*/
    }


//    fun printSelf(){
//
//    }
//
//    fun test(){
//        thread{
//            FTHelper.INSTANCE.disconnectBluetooth()
//            FTHelper.INSTANCE.disconnect()//配网时先确保断开之前连接
//            var con = FTHelper.INSTANCE.connectBluetooth("fc:58:fa:44:54:df".toUpperCase())
//            LogUtils.d("con = $con")
//        }
//    }

}