package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;


import java.util.List;

/**
 * Created by hasee on 2018/6/25.
 */

public class AssetsAdapter extends BaseAdapter {
    private List<DigitalEntity> currencyEntities;
    private LayoutInflater inflate;
    private Context context;
    private boolean isCiphertext;

    public AssetsAdapter(Context context, List<DigitalEntity> currencyEntities) {
        this.currencyEntities = currencyEntities;
        this.context = context;
        this.inflate = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return currencyEntities.size();
    }

    public void setCiphertext(boolean isCiphertext) {
        this.isCiphertext = isCiphertext;
    }

    @Override
    public DigitalEntity getItem(int position) {
        return currencyEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_currency, null);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.img_currency_icon);
            holder.tv_name = convertView.findViewById(R.id.tv_currency_name);
            holder.tv_assets = convertView.findViewById(R.id.tv_currency_assets);
            holder.tv_valus = convertView.findViewById(R.id.tv_currency_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageLoader.getInstance().displayImage(currencyEntities.get(position).getIconPath(), holder.imageView, DefApplication.getOptions());
        holder.tv_name.setText(currencyEntities.get(position).getCurrencyName());
        if (isCiphertext) {
            holder.tv_assets.setText("****");
            holder.tv_valus.setText("****");
        } else {
            holder.tv_assets.setText(currencyEntities.get(position).getHasCurrency());
            CommonUtils.setCurrencyShow(holder.tv_valus, context.getResources().getString(R.string.approximate_symbol) + context.getResources().getString(R.string.rmb_symbol) + currencyEntities.get(position).getCurrencyValue(), context.getResources().getString(R.string.approximate_symbol) + context.getResources().getString(R.string.usd_symbol) + currencyEntities.get(position).getCurrencyValue());
        }
        return convertView;

    }

    class ViewHolder {
        ImageView imageView;
        TextView tv_name;
        TextView tv_assets;
        TextView tv_valus;
    }
}
