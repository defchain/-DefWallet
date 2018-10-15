package com.lajoin.defwallet.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.Constants;

/**
 * Created by hasee on 2018/7/5.
 */

public class StartActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_creat_wallet, btn_import_wallet;
    private MBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        DefApplication.getCreateWalletActivityList().add(this);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        btn_creat_wallet = (Button) findViewById(R.id.btn_creat_wallet);
        btn_import_wallet = (Button) findViewById(R.id.btn_import_wallet);
        btn_creat_wallet.setOnClickListener(this);
        btn_import_wallet.setOnClickListener(this);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {
        mBroadcastReceiver = new MBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.FINISHSTARTACTIVITY);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return null;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_creat_wallet:
                startActivity(new Intent(this, CreatWalletActivity.class).putExtra("_type", 1));
                break;
            case R.id.btn_import_wallet:
                startActivity(new Intent(this, ImportWalletActivity.class).putExtra("_type", 1));
                break;
        }
    }

    class MBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.FINISHSTARTACTIVITY)) {
                finish();
            }
        }
    }
}
