package com.example.threadpooldemo;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hprt.lib_ft800.data.FTInfo;

public class DiscoveryAdapter extends BaseQuickAdapter<FTInfo, BaseViewHolder> {

    public DiscoveryAdapter() {
        super(R.layout.item_discovery_device);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, FTInfo ftInfo) {
        TextView tv_name = baseViewHolder.itemView.findViewById(R.id.tv_name);
        tv_name.setText(ftInfo.getName());

        TextView tv_ip = baseViewHolder.itemView.findViewById(R.id.tv_ip);
        tv_ip.setText(ftInfo.getIp());

        TextView tv_sn = baseViewHolder.itemView.findViewById(R.id.tv_sn);
        tv_sn.setText(ftInfo.getSn());
    }
}