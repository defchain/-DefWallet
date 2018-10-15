package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.view.AuthenticationDialog;
import com.lajoin.defwallet.view.WarningView;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/6/28.
 */

public class BackupWarningActivity extends BaseActivity {
    private static final int PASSWORDSUCCESS = 1;
    private static final int PASSWORDFAIL = 2;
    private static final int GETDEFWALLET = 3;
    private Button btn_backup;
    private AuthenticationDialog authenticationDialog;
    private String walletAddress;
    private DEFWalletHelper helper;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;
    private DEFWallet defWallet;
    /**
     * 1:startActivity open,2:Other open;
     */
    private int type;
    private Toolbar mToolbar;
    private boolean isCanBack;

    private WarningView warningView;
    private RelativeLayout rl_backup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_warning);
        DefApplication.getCreateWalletActivityList().add(this);
        type = getIntent().getIntExtra("_type", -1);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case PASSWORDSUCCESS:
                if (passwordEditText != null) {
                    passwordEditText.setText("");
                }
                startActivity(new Intent(this, CopyMnemonicActivity.class).putExtra("walletAddress", walletAddress).putExtra("_type", type));
                break;
            case PASSWORDFAIL:
                if (authenticationDialog != null) {
                    if (passwordEditText != null) {
                        passwordEditText.setText("");
                    }
                    if (authenticationDialog.isShowing()) {
                        authenticationDialog.dismiss();
                    }
                }
                warningView = new WarningView(this, R.string.password_wrong);
                CommonUtils.buildWarning(this, rl_backup, warningView, mHandler);
                break;
            case GETDEFWALLET:
                isCanBack = true;
                break;
            case Constants.SHOWWARNINGVIEW:
                CommonUtils.cancelWarning(this, warningView, rl_backup);
                break;

        }
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        walletAddress = intent.getStringExtra("walletAddress");
        helper = DEFWalletHelper.getInstance();
        progressDialog = new ProgressDialog(this);
        authenticationDialog = new AuthenticationDialog(this);
        isCanBack = false;
        mToolbar = findViewById(R.id.toolbar_backup_warning);
        rl_backup = findViewById(R.id.rl_backup);
        mToolbar.setNavigationIcon(R.mipmap.arrow_left);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        new Thread() {
            @Override
            public void run() {
                defWallet = helper.findWalletByAddress(walletAddress);
                if (defWallet != null) {
                    mHandler.sendEmptyMessage(GETDEFWALLET);
                }
            }
        }.start();
        btn_backup = findViewById(R.id.btn_backup);
        btn_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticationDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
                authenticationDialog.setCanceledOnTouchOutside(false);
                authenticationDialog.setCancelable(false);
                authenticationDialog.show();
                if (authenticationDialog.getEditText() != null) {
                    passwordEditText = authenticationDialog.getEditText();
                }
                if (authenticationDialog.getTv_ok() != null) {
                    authenticationDialog.getTv_ok().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (passwordEditText != null) {
                                CommonUtils.buildProgressDialog(BackupWarningActivity.this, progressDialog, R.string.validate_password);
                                new Thread() {
                                    @Override
                                    public void run() {
                                        if (helper.validatePassword(walletAddress, passwordEditText.getText().toString().trim())) {
                                            CommonUtils.cancelProgressDialog(progressDialog);
                                            authenticationDialog.dismiss();
                                            mHandler.sendEmptyMessage(PASSWORDSUCCESS);
                                        } else {
                                            CommonUtils.cancelProgressDialog(progressDialog);
                                            mHandler.sendEmptyMessage(PASSWORDFAIL);
                                        }
                                    }
                                }.start();
                            }
                        }
                    });
                }

                if (authenticationDialog.getTv_cancel() != null) {
                    authenticationDialog.getTv_cancel().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            authenticationDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_backup;
    }

    private void onBack() {
        if (isCanBack) {
            if (type == 1) {
                DefApplication.setCurrentUser(defWallet);
                sendBroadcast(new Intent(Constants.FINISHSTARTACTIVITY));
                startActivity(new Intent(BackupWarningActivity.this, MainActivity.class).putExtra("walletAddress", walletAddress));
                finish();
            } else {
                DefApplication.setCurrentUser(defWallet);
                finish();
            }
        }
    }
}
