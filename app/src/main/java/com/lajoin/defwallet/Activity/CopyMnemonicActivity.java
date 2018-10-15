package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.view.WarningDialog;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/6/28.
 */

public class CopyMnemonicActivity extends BaseActivity {
    private static final int GETMNEMONIC = 1;
    private String walletAddress;
    private Button btn_next;
    private TextView tv_mnemonic;
    private DEFWalletHelper helper;
    private ProgressDialog progressDialog;
    private DEFWallet defWallet;
    private WarningDialog warningDialog;
    private RelativeLayout rl_copy_mnemonic;
    /**
     * 1:startActivity open,2:Other open;
     */
    private int type;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copymnemonic);
        DefApplication.getCreateWalletActivityList().add(this);
        type = getIntent().getIntExtra("_type", -1);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case GETMNEMONIC:
                defWallet = (DEFWallet) msg.obj;
                tv_mnemonic.setText(defWallet.getMnemonic());
                btn_next.setEnabled(true);
                CommonUtils.cancelProgressDialog(progressDialog);
                CommonUtils.showDialog(warningDialog);
                break;
        }
    }

    @Override
    protected void initView() {
        progressDialog = new ProgressDialog(this);
        warningDialog = new WarningDialog(this);
        CommonUtils.buildProgressDialog(this, progressDialog, R.string.get_mnemonic);
        helper = DEFWalletHelper.getInstance();
        Intent intent = getIntent();
        walletAddress = intent.getStringExtra("walletAddress");
        mToolbar = findViewById(R.id.toolbar_copy_mnemonic);
        rl_copy_mnemonic = findViewById(R.id.rl_copy_mnemonic);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        tv_mnemonic = findViewById(R.id.tv_mnemonic);
        if (walletAddress != null && !walletAddress.equals("")) {
            new Thread() {
                @Override
                public void run() {
                    DEFWallet defWallet = helper.findWalletByAddress(walletAddress);
                    if (defWallet != null) {
                        Message msg = Message.obtain();
                        msg.what = GETMNEMONIC;
                        msg.obj = defWallet;
                        mHandler.sendMessage(msg);
                    }
                }
            }.start();
        }
        btn_next = findViewById(R.id.btn_next);
        btn_next.setEnabled(false);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CopyMnemonicActivity.this, ConfirmMnemonicActivity.class).putExtra("walletAddress", walletAddress).putExtra("_type", type));
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
        return rl_copy_mnemonic;
    }

}
