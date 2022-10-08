package com.example.threadpooldemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.threadpooldemo.Utils.FileUtil;
import com.example.threadpooldemo.Utils.LogThreadPool;
import com.example.threadpooldemo.Utils.ThreadExecutors;

import com.hprt.lib_ft800.FTHelper;
import com.hprt.lib_ft800.config.ErrorCode;
import com.hprt.lib_ft800.config.FT200;
import com.hprt.lib_ft800.data.FTStatus;
import com.hprt.lib_ft800.data.MileageModel;
import com.hprt.lib_ft800.exception.PrintException;
import com.hprt.lib_ft800.listener.*;
import com.hprt.lib_ft800.listener.NameListener;
import com.hprt.lib_pdf.image.ImagePool;
import com.hprt.lib_pdf.image.ProcessHelper;
import com.hprt.lib_pdf.model.data.ImageEntity;
import com.hprt.lib_pdf.model.data.ProcessMethod;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;


import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    private Context mContext;

    private static final int REQUEST_CODE_CHOOSE_PDF = 112;
    private static final int REQUEST_CODE_CHOOSE_ALL_FIRMWARE = 115;
    private static final int REQUEST_CODE_DISCOVERY_PRINTER = 116;

    private static final int PRINT_TYPE_FT200 = 1;
    private static final int PRINT_TYPE_FT800 = 2;

    private TextView mTvStatus;
    private TextView mTvMsg;
    private EditText mEtIp;
    private RecyclerView mRvContent;

    private File pdfFile;
    private File firmware;
    private ArrayList<String> cmds = new ArrayList<>();
    private String tmpname = "12345.pdf";
    private String LAST_IP = "LAST_IP";

    private BaseQuickAdapter adapter;

    public StatusListener stateListener;
    public PrintListener printListener;
    public WiFiListener wifiListener;
    public MileageListener mileageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mTvStatus = findViewById(R.id.tv_status);
        mTvMsg = findViewById(R.id.tv_msg);
        mEtIp = findViewById(R.id.et_ip);
        mRvContent = findViewById(R.id.recyclerview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EasyPermissions.requestPermissions(
                    MainActivity.this,
                    "申请权限",
                    0,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        } else{

        }

        stateListener = new StatusListener() {
            @Override
            public void onStatus(FTStatus ftStatus) {
                if (ftStatus.getCode() == 0){
                    ThreadExecutors.mainThread.execute(
                            new Runnable() {
                           @Override
                           public void run() {
                               mTvStatus.setText(TimeUtils.getNowString() + ":\n状态正常 空闲="+ ftStatus.getIdle());
                           }
                       }
                    );
                } else{
                    ThreadExecutors.mainThread.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                String msg = "";

                                for (ErrorCode code : ftStatus.getList()) {
                                    if (code == ErrorCode.OFFLINE) {
                                        msg += "脱机 ";
                                    } else if (code == ErrorCode.MISSPAPER) {
                                        msg += "缺纸 ";
                                    } else if (code == ErrorCode.UNCAP) {
                                        msg += "开盖 ";
                                    } else if (code == ErrorCode.HIGH_TEMPERATURE) {
                                        msg += "高温 ";
                                    } else if (code == ErrorCode.BATTERY_LOW) {
                                        msg += "电量低 ";
                                    } else if (code == ErrorCode.PAPER_NOT_PICK) {
                                        msg += "未取纸 ";
                                    } else if (code == ErrorCode.CUTTER_ERROR) {
                                        msg += "切刀错误 ";
                                    } else if (code == ErrorCode.POSTION_FAIL) {
                                        msg += "定位错误 ";
                                    } else if (code == ErrorCode.CACHED) {
                                        msg += "缓存非空 ";
                                    } else if (code == ErrorCode.ILLEGAL_SUPPLIES) {
                                        msg += "非法耗材 ";
                                    } else if (code == ErrorCode.SUPPLIES_END) {
                                        msg += "卷纸用尽 ";
                                    }
                                }
                                mTvStatus.setText(TimeUtils.getNowString() + ":\n 状态："+msg+" 空闲=" + ftStatus.getIdle());
                            }
                        }
                     );
                }
            }
        };

        wifiListener = new WiFiListener() {
            @Override
            public void onMode(int mode) {
                ThreadExecutors.mainThread.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                String currentMode = "";
                                if (mode == 0) {
                                    currentMode = "关闭";
                                } else if (mode == 1) {
                                    currentMode = "AP";
                                } else if (mode == 2) {
                                    currentMode = "STA";
                                }
                                mTvMsg.setText("mode = "+ currentMode);
                            }
                        }
                );
            }
            @Override
            public void onSSID(String ssid) {
                ThreadExecutors.mainThread.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                mTvMsg.setText("ssid = "+ ssid + "\n");
                            }
                        }
                );
            }
            @Override
            public void onIpAddress(String ip) {
                ThreadExecutors.mainThread.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                mTvMsg.setText("ssid = "+ ip + "\n");
                            }
                        }
                );
            }
        };


        mileageListener = new MileageListener() {
            @Override
            public void onMileage(MileageModel mileage) {
                LogUtils.d( "里程："+mileage);
            }
        };


        printListener = new PrintListener() {
            @Override
            public void onPrintSuccess(int id) {
                showMsg("打印完成");
            }

            public void onPrintFail(@NonNull PrintException e) {
                switch(e.getType()) {
                    case 0:
                        showMsg(e.getMessage()+" printcmd");
                        LogUtils.d(e.getMessage()+" printcmd");
                        break;
                    case 1:
                        showMsg(e.getMessage()+" printBitmap");
                        LogUtils.d(e.getMessage()+" printBitmap");
                }

            }

            public void onPrintSendFail(@NonNull Exception e) {
                showMsg(e.getMessage());
            }

            public void onUpgradeSuccess() {
                showMsg("升级成功");
            }

            public void onUpgradeFail(@NonNull Exception e) {
                showMsg(e.getMessage());
            }

            public void onUpgradeSendFail(@NonNull Exception e) {
                showMsg(e.getMessage());
            }
        };

        setListener();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    File tmpFile = new File(Utils.getApp().getExternalCacheDir().getAbsolutePath() +"/"+tmpname);
                    if(!tmpFile.exists()){
                        FileUtil.cpAssertToLocalPath(MainActivity.this, tmpname, Utils.getApp().getExternalCacheDir().getAbsolutePath() +"/"+tmpname);
                    }
                    pdfFile = new File(Utils.getApp().getExternalCacheDir().getAbsolutePath() + "/"+tmpname);
                    LogUtils.w(TAG, "init pdfFile="+pdfFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        if (!SPUtils.getInstance().getString(LAST_IP).isEmpty()){
            mEtIp.setText(SPUtils.getInstance().getString(LAST_IP));
        }

        addItems();

        adapter = new BaseQuickAdapter(R.layout.item_main, cmds) {
            @Override
            protected void convert(@NonNull BaseViewHolder baseViewHolder, Object o) {
                baseViewHolder.setText(R.id.text1, (String)o);
            }
        };

        mRvContent.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mRvContent.addItemDecoration(new DividerItemDecoration(mContext, LinearLayout.VERTICAL));
        mRvContent.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (cmds.get(i).equals(mContext.getResources().getString(R.string.connect))) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            FTHelper.INSTANCE.connect(mEtIp.getText().toString().trim(), new ConnectListener() {
                                @Override
                                public void onSuccess() {
                                    ThreadExecutors.mainThread.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTvMsg.setText("连接成功");
                                            SPUtils.getInstance().put(LAST_IP, mEtIp.getText().toString(), true);
                                        }
                                    });
                                }

                                @Override
                                public void onDeviceLost(Exception e) {
                                    ThreadExecutors.mainThread.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTvMsg.setText("设备丢失:"+e.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onFail(Exception e) {
                                    ThreadExecutors.mainThread.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTvMsg.setText("连接断开:"+e.getMessage());
                                        }
                                    });
                                }
                            });
                        }
                    }.start();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.disconnect))) {
                    FTHelper.INSTANCE.disconnect();
                    mTvMsg.setText("连接断开");
                    mTvStatus.setText("");
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.select_pdf))) {
                    String[] fileExts = new String[]{".pdf"};
                    LFilePicker filePicker = new LFilePicker();
                    filePicker.withActivity(MainActivity.this)
                            .withRequestCode(REQUEST_CODE_CHOOSE_PDF)
                            .withTitle("select pdf")
                            .withFileFilter(fileExts)
                            .withMutilyMode(false)
                            .start();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.print))) {
                    int [] itemId = {101,102,105,110,120,150};
                    String[] list = new String[]{"1","2","5","10","20","50"};
                    MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                            .title("打印张数")
                            .items(list)
                            .itemsIds(itemId)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    dialog.dismiss();
                                    int numbers = Integer.parseInt(list[position]);
                                    print(0, numbers);
                                }
                            }).build();
                    dialog.show();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.print_cut))) {
                    FTHelper.INSTANCE.setPrinterType(PRINT_TYPE_FT800);
                    print(1, 1);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.print_black_mark))) {
                    FTHelper.INSTANCE.setPrinterType(PRINT_TYPE_FT800);
                    print(3, 1);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.print_red_black))) {
                    FTHelper.INSTANCE.setPrinterType(PRINT_TYPE_FT800);
                    printRedAndBlack(true);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.battery))) {
                    FTHelper.INSTANCE.getBattery(new BatteryListener(){
                        @Override
                        public void onBattery(String battery) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("battery = "+ battery);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.version))) {
                    /*FTHelper.INSTANCE.getFirmwareVersion(new FirmwareListener() {
                        @Override
                        public void onVersion(String version) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("version = "+ version);
                                }
                            });
                        }
                    });*/
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.status))) {
                    FTHelper.INSTANCE.checkState();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.config_net))) {
                    startActivity(new Intent(mContext, ConfigNetActivity.class));
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.wifi_mode))) {
                    FTHelper.INSTANCE.getWifiMode(wifiListener);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.wifi_info))) {
                    FTHelper.INSTANCE.getWiFiInfo(wifiListener);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.wifi_version))) {
                    FTHelper.INSTANCE.getWiFiVersion(new WiFiVersionListener() {
                        @Override
                        public void onVersion(String version) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("WiFi Version = "+ version);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.model))) {
                    FTHelper.INSTANCE.getModel(new ModelListener() {
                        @Override
                        public void onModel(String mode) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("model  = "+ mode);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.name))) {
                    FTHelper.INSTANCE.getPrintName(new NameListener() {
                        @Override
                        public void onName(String name) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("name  = "+ name);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.sn))) {
                    FTHelper.INSTANCE.getPrinterNo(new SnListener() {
                        @Override
                        public void getSn(String sn) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("sn  = "+ sn);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.upgrade))) {
                    String[] fileExts = new String[]{".bin"};
                    LFilePicker filePicker = new LFilePicker();
                    filePicker.withActivity(MainActivity.this)
                            .withRequestCode(REQUEST_CODE_CHOOSE_ALL_FIRMWARE)
                            .withTitle("select bin")
                            .withFileFilter(fileExts)
                            .withMutilyMode(false)
                            .start();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.getStandByTime))) {
                    FTHelper.INSTANCE.getStandByTime(new StandbyTimeListener() {
                        @Override
                        public void onTime(int time) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("time  = "+ time + "s");
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.setStandByTime))) {
                    String[] list = new String[]{"5分钟","10分钟","30分钟","1小时","永不"};
                    MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                            .title("设置待机时间")
                            .items(list)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    dialog.dismiss();

                                    int time = 0;
                                    if (position == 0) {
                                        time = 5*60;
                                    } else if (position == 0) {
                                        time = 10*60;
                                    } else if (position == 0) {
                                        time = 30*60;
                                    } else if (position == 0) {
                                        time = 60*60;
                                    } else if (position == 0) {
                                        time = 0;
                                    }
                                    FTHelper.INSTANCE.setStandbyTime(time);
                                }
                            }).build();
                    dialog.show();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.getDensity))) {
                    FTHelper.INSTANCE.getDensity(new DensityListener() {
                        @Override
                        public void onDensity(int density) {
                            ThreadExecutors.mainThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTvMsg.setText("density  = "+ density);
                                }
                            });
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.setDensity))) {
                    String[] list = new String[]{"低","中","高"};
                    MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                            .title("设置浓度")
                            .items(list)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    dialog.dismiss();
                                    FTHelper.INSTANCE.setDensity(position+1);
                                }
                            }).build();
                    dialog.show();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.discovery))) {
                    startActivityForResult(new Intent(mContext, DiscoveryListActivity.class), REQUEST_CODE_DISCOVERY_PRINTER);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.tianmaovolume))) {
                    setTianmaoVolume();
//                    var test = byteArrayOf(0x11,0x22,0x33,0x44);
//                    var result = CrcUtils.crc32(test);
//                    LogUtils.d("crc result = "+ByteUtils.bytetohex(result))
//                    tv_msg.text = ByteUtils.bytetohex(result)
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.cancel))) {
                    FTHelper.INSTANCE.disconnectPrint();
                    FTHelper.INSTANCE.cancelPrint();
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.consumable_remaining))) {
                    FTHelper.INSTANCE.getConsumableRemaining(new ConsumableListener() {
                        @Override
                        public void onConsumable(int len) {
                            showMsg("碳带余量:" + len);
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.print_width))) {
                    FTHelper.INSTANCE.getPrintWidth(new PrintWidthListener() {
                        @Override
                        public void onWidth(int width) {
                            showMsg("打印宽度:"+ width);
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.consumable_model))) {
                    FTHelper.INSTANCE.getConsumableModel(new ConsumableModelListener() {
                        @Override
                        public void onConsumableModel(String model) {
                            showMsg("耗材型号:"+ model);
                        }
                    });
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.dpi200))) {
                    FTHelper.INSTANCE.setPrinterType(PRINT_TYPE_FT200);
                    print(1, 1);
                } else if (cmds.get(i).equals(mContext.getResources().getString(R.string.paper_type))) {
                    selectPaperType();
                }
            }
        });


        /*  ThreadTask test */
        /*
        for (int i = 0; i < 3; i++) {
            LogThreadPool.executorService.execute(new ThreadTask());
        }*/
    }

    private void setListener() {
        FTHelper.INSTANCE.setStatusListener(stateListener);
        FTHelper.INSTANCE.setPrintListener(printListener);
    }

    public final void showMsg(@NonNull final String msg) {
        ThreadExecutors.mainThread.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        mTvMsg.setText((CharSequence) msg);
                    }
                }
        );
    }

    private void addItems(){
        cmds.add(mContext.getResources().getString(R.string.connect));
        cmds.add(mContext.getResources().getString(R.string.disconnect));
        cmds.add(mContext.getResources().getString(R.string.select_pdf));
        cmds.add(mContext.getResources().getString(R.string.print));
        cmds.add(mContext.getResources().getString(R.string.print_cut));
        cmds.add(mContext.getResources().getString(R.string.print_black_mark));
        cmds.add(mContext.getResources().getString(R.string.version));
        cmds.add(mContext.getResources().getString(R.string.battery));
        cmds.add(mContext.getResources().getString(R.string.status));
        cmds.add(mContext.getResources().getString(R.string.config_net));
        cmds.add(mContext.getResources().getString(R.string.wifi_mode));
        cmds.add(mContext.getResources().getString(R.string.wifi_info));
        cmds.add(mContext.getResources().getString(R.string.wifi_version));
        cmds.add(mContext.getResources().getString(R.string.model));
        cmds.add(mContext.getResources().getString(R.string.name));
        cmds.add(mContext.getResources().getString(R.string.sn));
        cmds.add(mContext.getResources().getString(R.string.upgrade));
        cmds.add(mContext.getResources().getString(R.string.getStandByTime));
        cmds.add(mContext.getResources().getString(R.string.setStandByTime));
        cmds.add(mContext.getResources().getString(R.string.getDensity));
        cmds.add(mContext.getResources().getString(R.string.setDensity));
        cmds.add(mContext.getResources().getString(R.string.discovery));
        cmds.add(mContext.getResources().getString(R.string.tianmaovolume));
        cmds.add(mContext.getResources().getString(R.string.cancel));
        cmds.add(mContext.getResources().getString(R.string.consumable_remaining));
        cmds.add(mContext.getResources().getString(R.string.print_width));
        cmds.add(mContext.getResources().getString(R.string.consumable_model));
        cmds.add(mContext.getResources().getString(R.string.dpi200));
        cmds.add(mContext.getResources().getString(R.string.paper_type));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHOOSE_PDF) {
                LogUtils.w(TAG, "onActivityResult REQUEST_CODE_CHOOSE_PDF");
                ArrayList<String> pathArray = new ArrayList<>();
                pathArray = data.getStringArrayListExtra(Constant.RESULT_INFO);
                if (!pathArray.isEmpty()) {
                    String pdfPath = pathArray.get(0);
                    pdfFile = new File(pdfPath);
                    mTvMsg.setText(pdfFile.getName());
                } else {
                    ToastUtils.showShort("select fail");
                }
            } else if (requestCode == REQUEST_CODE_CHOOSE_ALL_FIRMWARE) {
                ArrayList<String> pathArray2 = new ArrayList<>();
                pathArray2 = data.getStringArrayListExtra(Constant.RESULT_INFO);
                if (!pathArray2.isEmpty()) {
                    String pdfPath2 = pathArray2.get(0);

                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            File file = new File(pdfPath2);
                            try {
                                FileInputStream is = new FileInputStream(file);
                                FTHelper.INSTANCE.upgradePrinterAndWiFi(is);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    ToastUtils.showShort("select fail");
                }
            } else if (requestCode == REQUEST_CODE_DISCOVERY_PRINTER) {
                String ip = data.getStringExtra("ip");
                mEtIp.setText(ip);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 打印后是否切刀
     * 0 不切，1 切刀 3黑标
     */
    private void print(int isCut, int numbers){
        LogUtils.w(TAG, "print pdfFile="+pdfFile.getAbsolutePath());

        if (!FileUtils.isFileExists(pdfFile)) {
            ToastUtils.showShort("请选择pdf文件");
            return;
        }
        ToastUtils.showShort("开始发送");

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    FTHelper.INSTANCE.preparePrint();
                    LogUtils.w(TAG, "print numbers="+numbers+", isCut="+isCut);
                    for (int i = 1; i <= numbers; i ++) {
                        int taskId = i;

                        LogUtils.w(TAG, "print pdfFile="+pdfFile);
                        ProcessHelper.INSTANCE.loadingPdf(pdfFile);

                        ImageEntity imageEntity = new ImageEntity(0, new ProcessMethod(1, true));
                        ImagePool.INSTANCE.enqueue(pdfFile.getPath(), null, imageEntity);

                        LogUtils.w(TAG, "print pdfToPrintImage");
                        //Bitmap printBitmap = FTHelper.INSTANCE.pdfToPrintImage(pdfFile, 0);

                        while(true) {
                            if(FTHelper.INSTANCE.isEnableSend()){
                                break;
                            }
                        }

                        LogUtils.w(TAG, "imageEntity.getResultPath()="+imageEntity.getResultPath());
                        Bitmap printBitmap = BitmapFactory.decodeFile(imageEntity.getResultPath());

                        //val path = Utils.getApp().getExternalFilesDir("tmp")!!.absolutePath+"/test.png"
                        //ImageUtils.save(printBitmap, path, Bitmap.CompressFormat.PNG, false)
//                    var printBitmap = FTHelper.INSTANCE.createPdfImageBgByPage(pdfFile!!, 0)



                        int ctl = isCut;
                        boolean printCmdResult = FTHelper.INSTANCE.printCmd(
                                taskId, 1, 1, printBitmap.getWidth(), printBitmap.getHeight(), (byte)ctl, 1, taskId);
                        if (printCmdResult) {
                            boolean result = FTHelper.INSTANCE.printBitmap(printBitmap, taskId, 1, false, true);
                            if (result) {
                                showMsg("发送完成，正在打印");
                            } else {
                                showMsg("数据打印执行失败");
                            }
                        } else {
                            showMsg("位图命令执行失败");
                        }
                        //云打印测试
                        //var data = FTHelper.INSTANCE.yunData(1,1,printBitmap,1,1,false)
                        //FTHelper.INSTANCE.sendData(data, 30*1000)
                    }
                    FTHelper.INSTANCE.disconnectPrint();
                } catch (Throwable t){
                    /*if (t instanceof PrintException)*/ {
                        showMsg(t.getMessage()+" printBitmap --print");
                        LogUtils.d(t.getMessage()+" printBitmap ---print");
                    }
                }
            }
        }.start();
    }

    public void setTianmaoVolume() {
        String[] list = new String[]{"10","20","30","40","50","60","70","80","90","100"};
        MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                .title("设置音量")
                .items(list)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        dialog.dismiss();
                        int volume = Integer.parseInt(list[position]);
                        FTHelper.INSTANCE.setTianmaoVolume(volume);
                    }
                }).build();
        dialog.show();
    }

    public void selectPaperType() {
        String[] list = new String[]{"连续纸","黑标纸"};
        MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                .title("纸张类型")
                .items(list)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        dialog.dismiss();
                        FTHelper.INSTANCE.setPaper(position);
                    }
                }).build();
        dialog.show();
    }

    private void printRedAndBlack(boolean isCut){
        if(!FileUtils.isFileExists("/sdcard/by.pdf")){
            ToastUtils.showShort("请选择pdf文件");
            return;
        }
        ToastUtils.showShort("开始发送");

        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap printBitmap = FTHelper.INSTANCE.pdfToPrintImage(new File("/sdcard/by.pdf"), 0);
                ImageUtils.save(
                        printBitmap,
                        Utils.getApp().getExternalFilesDir(null).getAbsolutePath() + "/0.jpg",
                        Bitmap.CompressFormat.JPEG);
                ArrayList<Bitmap> list = FTHelper.INSTANCE.test(printBitmap);
                if (list.size() == 2) {
                    ImageUtils.save(
                            list.get(0),
                            Utils.getApp().getExternalFilesDir(null).getAbsolutePath() + "/1.jpg",
                            Bitmap.CompressFormat.JPEG,
                            true);
                    ImageUtils.save(
                            list.get(1),
                            Utils.getApp().getExternalFilesDir(null).getAbsolutePath() + "/2.jpg",
                            Bitmap.CompressFormat.JPEG,
                            true);
                }
            }
        }.start();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been granted

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been denied
    }




    /**************************** ThreadTask*************************************/
    public class ThreadTask implements Runnable {
        public ThreadTask() {

        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

