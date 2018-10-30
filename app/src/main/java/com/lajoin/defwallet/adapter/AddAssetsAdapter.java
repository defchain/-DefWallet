package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.utils.WalletAssetsSharedPreferencesUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class AddAssetsAdapter extends BaseAdapter {
    private List<DigitalEntity> allDigitalEntityList;
    private Context context;
    private LayoutInflater inflate;
    private List<DigitalEntity> mDigitalEntityList;

    public AddAssetsAdapter(Context context, List<DigitalEntity> allDigitalEntityList, List<DigitalEntity> mDigitalEntityList) {
        this.context = context;
        this.allDigitalEntityList = allDigitalEntityList;
        this.mDigitalEntityList = mDigitalEntityList;
        this.inflate = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return allDigitalEntityList.size();
    }

    @Override
    public DigitalEntity getItem(int position) {
        return allDigitalEntityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_add_assets, null);
            holder = new ViewHolder();
            holder.tv_add_assets_name = convertView.findViewById(R.id.tv_add_assets_name);
            holder.img_add_assets_icon = convertView.findViewById(R.id.img_add_assets_icon);
            holder.switch_add_assets = convertView.findViewById(R.id.switch_add_assets);
            holder.rl_item_add_assets = convertView.findViewById(R.id.rl_item_add_assets);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader.getInstance().displayImage(allDigitalEntityList.get(position).getIconPath(), holder.img_add_assets_icon, DefApplication.getOptions());
        holder.tv_add_assets_name.setText(allDigitalEntityList.get(position).getCurrencyName());
        holder.switch_add_assets.setChecked(false);
        for (int i = 0; i < mDigitalEntityList.size(); i++) {
            if (mDigitalEntityList.get(i).getCurrencyName().equals(allDigitalEntityList.get(position).getCurrencyName())) {
                holder.switch_add_assets.setChecked(true);
            }
        }
        holder.rl_item_add_assets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.switch_add_assets.isChecked()) {
                    holder.switch_add_assets.setChecked(false);
                    WalletAssetsSharedPreferencesUtils.getInstance(context).deleteDigitalRecode(DefApplication.getCurrentUser().getAddress(), allDigitalEntityList.get(position).getCurrencyName());
                } else {
                    holder.switch_add_assets.setChecked(true);
                    WalletAssetsSharedPreferencesUtils.getInstance(context).save(allDigitalEntityList.get(position), DefApplication.getCurrentUser().getAddress());
                }
            }
        });

        holder.switch_add_assets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.switch_add_assets.isChecked()) {
                    WalletAssetsSharedPreferencesUtils.getInstance(context).save(allDigitalEntityList.get(position), DefApplication.getCurrentUser().getAddress());
                } else {
                    WalletAssetsSharedPreferencesUtils.getInstance(context).deleteDigitalRecode(DefApplication.getCurrentUser().getAddress(), allDigitalEntityList.get(position).getCurrencyName());
                }
            }
        });


        return convertView;
    }

    class ViewHolder {
        ImageView img_add_assets_icon;
        TextView tv_add_assets_name;
        Switch switch_add_assets;
        RelativeLayout rl_item_add_assets;
    }
}
