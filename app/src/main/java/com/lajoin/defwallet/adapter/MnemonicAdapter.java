package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.MnemonicButtonEntity;

import java.util.List;

/**
 * Created by hasee on 2018/6/29.
 */

public class MnemonicAdapter extends BaseAdapter {
    private List<MnemonicButtonEntity> mnemonicButtonEntitys;
    private LayoutInflater inflate;
    private Context context;

    public MnemonicAdapter(Context context, List<MnemonicButtonEntity> mnemonicButtonEntitys) {
        this.mnemonicButtonEntitys = mnemonicButtonEntitys;
        this.inflate = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mnemonicButtonEntitys.size();
    }

    @Override
    public Object getItem(int position) {
        return mnemonicButtonEntitys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_button, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.item_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mnemonicButtonEntitys.get(position).getTextConten());
        if (mnemonicButtonEntitys.get(position).getType() == 1) {
            holder.textView.setBackground(context.getResources().getDrawable(R.drawable.shape_button4));
            holder.textView.setTextColor(context.getResources().getColor(R.color.bottom_gray));
        } else if (mnemonicButtonEntitys.get(position).getType() == 2) {
            holder.textView.setBackground(context.getResources().getDrawable(R.drawable.shape_button5));
            holder.textView.setTextColor(context.getResources().getColor(R.color.white));
        } else if (mnemonicButtonEntitys.get(position).getType() == 3) {
            holder.textView.setTextColor(Color.TRANSPARENT);
            holder.textView.setTextColor(context.getResources().getColor(R.color.bottom_gray));
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView;
    }
}
