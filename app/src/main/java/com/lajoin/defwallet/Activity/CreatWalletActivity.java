package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CheckStringUtils;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.view.WarningView;


import java.util.List;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;


/**
 * Created by hasee on 2018/6/28.
 */

public class CreatWalletActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private static final int CREATEWALLETNOW = 1;
    private List<DEFWallet> mDEFWalletList;
    private String walletName, path, passWord;
    private boolean isSameName;
    private Button btn_star_creat;
    private EditText edt_wallet_name, edt_cretea_wallet_password, edt_cretea_wallet_repassword;
    private ImageView img_password_ciphertext, img_repassword_ciphertext;
    private DEFWalletHelper helper;
    private ProgressDialog progressDialog;
    private WarningView warningView;
    private RelativeLayout rl_cretae_wallet;

    private String walletAddress;
    private Toolbar mToolbar;

    /**
     * 1:startActivity open,2:Other open;
     */
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creat_wallet);
        DefApplication.getCreateWalletActivityList().add(this);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case CREATEWALLETNOW:
                walletAddress = (String) msg.obj;
                startActivity(new Intent(this, BackupWarningActivity.class).putExtra("walletAddress", walletAddress).putExtra("_type", type));
                CommonUtils.createDefaultDiaital(this, walletAddress);
                finish();
                break;
            case Constants.SHOWWARNINGVIEW:
                CommonUtils.cancelWarning(this, warningView, rl_cretae_wallet);
                break;
            case Constants.GETLISTSUCCESS:
                for (int i = 0; i < mDEFWalletList.size(); i++) {
                    if (mDEFWalletList.get(i).getWalletName().equals(walletName)) {
                        isSameName = true;
                    }
                }
                if (!isSameName) {
                    new Thread() {
                        @Override
                        public void run() {
                            String address = helper.createNewWallet(walletName, passWord, "");
                            Log.e("defwallet", "create wallet:0x" + address);
                            if (address != null && !address.equals("")) {
                                CommonUtils.cancelProgressDialog(progressDialog);
                                Message message = Message.obtain();
                                message.what = CREATEWALLETNOW;
                                message.obj = address;
                                mHandler.sendMessage(message);
                            }
                        }
                    }.start();
                } else {
                    CommonUtils.cancelProgressDialog(progressDialog);
                    warningView = new WarningView(this, R.string.create_has_same_name);
                    CommonUtils.buildWarning(this, rl_cretae_wallet, warningView, mHandler);
                }
                break;
            case Constants.GETLISTFAILED:
                CommonUtils.cancelProgressDialog(progressDialog);
                warningView = new WarningView(this, getResources().getString(R.string.create_wallet_failed));
                CommonUtils.buildWarning(this, rl_cretae_wallet, warningView, mHandler);
                break;
        }
    }

    @Override
    protected void initView() {
        type = getIntent().getIntExtra("_type", -1);
        helper = DEFWalletHelper.getInstance();
        progressDialog = new ProgressDialog(this);
        mToolbar = findViewById(R.id.toolbar_creat_wallet);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        btn_star_creat = findViewById(R.id.btn_star_creat);
        btn_star_creat.setOnClickListener(this);
        edt_wallet_name = findViewById(R.id.edt_wallet_name);
        edt_cretea_wallet_password = findViewById(R.id.edt_cretea_wallet_password);
        edt_cretea_wallet_repassword = findViewById(R.id.edt_cretea_wallet_repassword);
        img_password_ciphertext = findViewById(R.id.img_password_ciphertext);
        img_repassword_ciphertext = findViewById(R.id.img_repassword_ciphertext);
        rl_cretae_wallet = findViewById(R.id.rl_cretae_wallet);
        img_password_ciphertext.setOnClickListener(this);
        img_repassword_ciphertext.setOnClickListener(this);

        edt_wallet_name.addTextChangedListener(this);
        edt_cretea_wallet_password.addTextChangedListener(this);
        edt_cretea_wallet_repassword.addTextChangedListener(this);

        btn_star_creat.setEnabled(false);
        edt_cretea_wallet_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edt_cretea_wallet_repassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_cretae_wallet;
    }


    private boolean isPwdCanSee = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_star_creat:
                isSameName = false;
                walletName = edt_wallet_name.getText().toString().trim();
                passWord = edt_cretea_wallet_password.getText().toString().trim();
                if (CheckStringUtils.isUserName(walletName)) {
                    if (passWord.equals(edt_cretea_wallet_repassword.getText().toString().trim())) {
                        if (CheckStringUtils.isVerification(passWord)) {
                            CommonUtils.buildProgressDialog(this, progressDialog, R.string.create_wallet_now);
                            new Thread() {
                                @Override
                                public void run() {
                                    mDEFWalletList = helper.listAllWallets();
                                    if (mDEFWalletList != null) {
                                        mHandler.sendEmptyMessage(Constants.GETLISTSUCCESS);
                                    } else {
                                        mHandler.sendEmptyMessage(Constants.GETLISTFAILED);
                                    }
                                }
                            }.start();
                        } else {
                            //password_format_wrong
                            warningView = new WarningView(this, R.string.password_format_wrong);
                            CommonUtils.buildWarning(this, rl_cretae_wallet, warningView, mHandler);
                        }
                    } else {
                        //password_different
                        warningView = new WarningView(this, R.string.password_different);
                        CommonUtils.buildWarning(this, rl_cretae_wallet, warningView, mHandler);
                    }
                } else {
                    //walletName_format_wrong
                    warningView = new WarningView(this, R.string.username_format_wrong);
                    CommonUtils.buildWarning(this, rl_cretae_wallet, warningView, mHandler);
                }
                break;
            case R.id.img_password_ciphertext:
                setPasswordEdit();
                break;
            case R.id.img_repassword_ciphertext:
                setPasswordEdit();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        btn_star_creat.setEnabled(false);
        btn_star_creat.setBackgroundResource(R.drawable.shape_button3);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!edt_wallet_name.getText().toString().equals("") && !edt_cretea_wallet_password.getText().toString().equals("") && !edt_cretea_wallet_repassword.getText().toString().equals("")) {
            btn_star_creat.setEnabled(true);
            btn_star_creat.setBackgroundResource(R.drawable.shape_button);
        }
    }

    private void setPasswordEdit() {
        if (!isPwdCanSee) {
            img_password_ciphertext.setImageResource(R.drawable.eye_open);
            edt_cretea_wallet_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            img_repassword_ciphertext.setImageResource(R.drawable.eye_open);
            edt_cretea_wallet_repassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isPwdCanSee = true;
        } else {
            img_password_ciphertext.setImageResource(R.drawable.eye_close);
            edt_cretea_wallet_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            img_repassword_ciphertext.setImageResource(R.drawable.eye_close);
            edt_cretea_wallet_repassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPwdCanSee = false;
        }
        edt_cretea_wallet_password.postInvalidate();
        CharSequence text = edt_cretea_wallet_password.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
        edt_cretea_wallet_repassword.postInvalidate();
        CharSequence text1 = edt_cretea_wallet_repassword.getText();
        if (text1 instanceof Spannable) {
            Spannable spanText = (Spannable) text1;
            Selection.setSelection(spanText, text1.length());
        }
    }
}
