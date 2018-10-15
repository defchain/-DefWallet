package com.lajoin.defwallet.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.QRCodeUtil;

import java.io.File;

/**
 * Created by xgp on 2018/7/2.
 */

public class ReceivablesActivity extends BaseActivity {
    private String filePath;
    private ImageView img_reveiver_qrcode;
    private TextView tv_address, tv_wallet_name;
    private RelativeLayout rl_copy_address;
    private Toolbar mToolbar;
    private RelativeLayout rl_receivables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivables);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case 1:
                img_reveiver_qrcode.setImageBitmap(BitmapFactory.decodeFile(filePath));
                break;
        }
    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_creat_wallet);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        Intent intent = getIntent();
        final String address = intent.getStringExtra("address");
        String walletName = intent.getStringExtra("walletName");
        img_reveiver_qrcode = findViewById(R.id.img_reveiver_qrcode);
        tv_address = findViewById(R.id.tv_address);
        tv_wallet_name = findViewById(R.id.tv_wallet_name);
        rl_copy_address = findViewById(R.id.rl_copy_address);
        rl_receivables = findViewById(R.id.rl_receivables);
        if (address.length() > 25) {
            String front = address.substring(0, 9);
            String after = address.substring(address.length() - 15, address.length());
            tv_address.setText(front + "******" + after);
        }
        tv_wallet_name.setText(walletName);
        filePath = CommonUtils.getFileRoot(this)
                + File.separator + "qr_" + System.currentTimeMillis()
                + ".jpg";
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean success = QRCodeUtil.createQRImage(
                        address.trim(),
                        800,
                        800,
                        null,
                        filePath);

                if (success) {
                    mHandler.sendEmptyMessage(1);
                }
            }
        }).start();

        rl_copy_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", address);
                cm.setPrimaryClip(mClipData);
                showToast(R.string.copy_success);
            }
        });
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_receivables;
    }


}
