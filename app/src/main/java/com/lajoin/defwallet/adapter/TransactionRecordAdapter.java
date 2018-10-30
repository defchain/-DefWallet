package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.TransactionResultEntity;

import java.util.List;

public class TransactionRecordAdapter extends BaseAdapter {
    private List<TransactionResultEntity> list;
    private Context context;
    private LayoutInflater inflate;

    public TransactionRecordAdapter(Context context, List<TransactionResultEntity> list) {
        this.list = list;
        this.context = context;
        this.inflate = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_tansaction, null);
            holder = new ViewHolder();
            holder.iv_tansaction = convertView.findViewById(R.id.iv_tansaction);
            holder.tv_transaction_address = convertView.findViewById(R.id.tv_transaction_address);
            holder.tv_transaction_time = convertView.findViewById(R.id.tv_transaction_time);
            holder.tv_transaction_value = convertView.findViewById(R.id.tv_transaction_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.get(position).isSend()) {
            holder.iv_tansaction.setImageResource(R.drawable.send_transaction);
            holder.tv_transaction_value.setTextColor(context.getResources().getColor(R.color.send_color));
            holder.tv_transaction_value.setText("-" + list.get(position).getValue());
        } else {
            holder.iv_tansaction.setImageResource(R.drawable.receive_transaction);
            holder.tv_transaction_value.setTextColor(context.getResources().getColor(R.color.receive_color));
            holder.tv_transaction_value.setText("+" + list.get(position).getValue());
        }
        holder.tv_transaction_address.setText(list.get(position).getMyAddress());
        holder.tv_transaction_time.setText(list.get(position).getTime());


        return convertView;
    }

    class ViewHolder {
        ImageView iv_tansaction;
        TextView tv_transaction_address;
        TextView tv_transaction_time;
        TextView tv_transaction_value;
    }
}
