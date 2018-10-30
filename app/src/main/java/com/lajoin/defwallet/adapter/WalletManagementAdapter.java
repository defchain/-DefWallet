package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.WalletEntity;

import java.util.List;

/**
 * Created by hasee on 2018/7/3.
 */

public class WalletManagementAdapter extends BaseAdapter {
    private List<WalletEntity> walletEntities;
    private Context context;
    private LayoutInflater inflate;
    private int bgs[] = new int[3];

    public WalletManagementAdapter(Context context, List<WalletEntity> walletEntities) {
        this.walletEntities = walletEntities;
        this.inflate = LayoutInflater.from(context);
        this.context = context;
        bgs[0] = R.drawable.shape_item_wallet1;
        bgs[1] = R.drawable.shape_item_wallet2;
        bgs[2] = R.drawable.shape_item_wallet3;
    }

    @Override
    public int getCount() {
        return walletEntities.size();
    }

    @Override
    public WalletEntity getItem(int position) {
        return walletEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_wallet, null);
            holder = new ViewHolder();
            holder.walletName = convertView.findViewById(R.id.item_wallet_name);
            holder.walletAddress = convertView.findViewById(R.id.item_wallet_address);
            holder.walletValue = convertView.findViewById(R.id.item_wallet_value);
            holder.walletColorful = convertView.findViewById(R.id.rl_wallet_colorful);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.walletName.setText(walletEntities.get(position).getWalletName());
        if (walletEntities.get(position).getWalletAddress().length() > 25) {
            String front = walletEntities.get(position).getWalletAddress().substring(0, 9);
            String after = walletEntities.get(position).getWalletAddress().substring(walletEntities.get(position).getWalletAddress().length() - 15, walletEntities.get(position).getWalletAddress().length());
            holder.walletAddress.setText(front + "..." + after);
        }
        String value;
        if (walletEntities.get(position).getWalletValue() != null) {
            value = walletEntities.get(position).getWalletValue();
        } else {
            value = "0.00";
        }

        String string = String.format(context.getResources().getString(R.string.total_assets_item_value), walletEntities.get(position).getWalletCurrencyType(), value);
        holder.walletValue.setText(string);
        holder.walletColorful.setBackgroundResource(bgs[position % 3]);
        return convertView;
    }

    class ViewHolder {
        TextView walletName;
        TextView walletAddress;
        TextView walletValue;
        RelativeLayout walletColorful;
    }
}
