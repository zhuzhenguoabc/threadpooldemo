package com.example.threadpooldemo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.threadpooldemo.Utils.ThreadExecutors;
import com.hprt.lib_ft800.data.FTInfo;
import com.hprt.lib_ft800.listener.FindListener;
import com.hprt.lib_ft800.utils.FTDiscovery;

public class DiscoveryListActivity extends AppCompatActivity {
    private static final String TAG = "DiscoveryListActivity";
    private Context mContext;

    private RecyclerView recyclerview;

    private DiscoveryAdapter rvAdapter;

    private FTDiscovery ftDiscovery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mContext = this;

        ftDiscovery = new FTDiscovery(this);

        rvAdapter = new DiscoveryAdapter();
        rvAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Intent intent = new Intent();
                intent.putExtra("ip", rvAdapter.getData().get(i).getIp());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerview.setAdapter(rvAdapter);
        discovery();
    }

    public void discovery() {
        ftDiscovery.startDiscovery(new FindListener() {
            @Override
            public void onFound(FTInfo ftinfo) {
                ThreadExecutors.mainThread.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if(!rvAdapter.getData().contains(ftinfo)) {
                                    rvAdapter.addData(ftinfo);
                                }
                            }
                        }
                );
            }

        });
    }


    @Override
    protected void onDestroy() {
        ftDiscovery.stopDiscovery();
        super.onDestroy();
    }
}