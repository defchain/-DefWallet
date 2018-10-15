package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;

import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/7/4.
 */

public class ExportKeystoreActivity extends BaseActivity {
    private static final int EXPORTKEYSTORESUCCESS = 1;
    private static final int EXPORTKEYSTOREFAILED = 2;
    private DEFWalletHelper helper;
    private ProgressDialog progressDialog;
    private TextView keystore_value;
    private Button btn_copy_keystore;
    private String walletAddress;
    private String keyStore;
    private Toolbar mToolbar;
    private RelativeLayout rl_export_keystore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_keystore);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case EXPORTKEYSTORESUCCESS:
                CommonUtils.cancelProgressDialog(progressDialog);
                keystore_value.setText(keyStore);
                btn_copy_keystore.setEnabled(true);
                btn_copy_keystore.setBackgroundResource(R.drawable.shape_button);
                btn_copy_keystore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", keyStore);
                        cm.setPrimaryClip(mClipData);
                        showToast(R.string.copy_success);
                    }
                });
                break;
            case EXPORTKEYSTOREFAILED:
                showToast(R.string.export_keystore_failed);
                break;
        }
    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_export_keystore);
        rl_export_keystore = findViewById(R.id.rl_export_keystore);
        CommonUtils.buildDefaultToolbar(this, mToolbar);

        walletAddress = getIntent().getStringExtra("walletAddress");

        helper = DEFWalletHelper.getInstance();
        progressDialog = new ProgressDialog(this);

        keystore_value = (TextView) findViewById(R.id.keystore_value);
        btn_copy_keystore = (Button) findViewById(R.id.btn_copy_keystore);
        btn_copy_keystore.setEnabled(false);
        btn_copy_keystore.setBackgroundResource(R.drawable.shape_button3);

        CommonUtils.buildProgressDialog(this, progressDialog, R.string.exporting_keystore);
        new Thread() {
            @Override
            public void run() {
                if (walletAddress != null && !walletAddress.equals("")) {
                    keyStore = helper.exportWalletKeystore(walletAddress);
                    if (keyStore != null && !keyStore.equals("")) {
                        mHandler.sendEmptyMessage(EXPORTKEYSTORESUCCESS);
                    } else {
                        mHandler.sendEmptyMessage(EXPORTKEYSTOREFAILED);
                    }
                }
            }
        }.start();
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_export_keystore;
    }

}
