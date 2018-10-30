package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.ChangeWalletItemEntity;
import com.lajoin.defwallet.utils.CommonUtils;

import java.util.List;

/**
 * Created by hasee on 2018/7/5.
 */

public class ChangeWalletAdapter extends BaseAdapter {
    private List<ChangeWalletItemEntity> changeWalletItemEntityList;
    private Context context;
    private LayoutInflater inflate;

    public ChangeWalletAdapter(Context context, List<ChangeWalletItemEntity> changeWalletItemEntityList) {
        this.changeWalletItemEntityList = changeWalletItemEntityList;
        this.context = context;
        this.inflate = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return changeWalletItemEntityList.size();
    }

    @Override
    public ChangeWalletItemEntity getItem(int position) {
        return changeWalletItemEntityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflate.inflate(R.layout.item_change_wallet, null);
            holder.textView = convertView.findViewById(R.id.item_change_wallet_name);
            holder.rl_change_wallet_item = convertView.findViewById(R.id.rl_change_wallet_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(CommonUtils.showWalletName(changeWalletItemEntityList.get(position).getWalletName()));
        if (changeWalletItemEntityList.get(position).isSelected()) {
            holder.rl_change_wallet_item.setBackgroundColor(context.getResources().getColor(R.color.wallet_item_selected));
        } else {
            holder.rl_change_wallet_item.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView;
        RelativeLayout rl_change_wallet_item;
    }

}
