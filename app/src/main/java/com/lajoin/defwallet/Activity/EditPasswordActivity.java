package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CheckStringUtils;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;

import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/7/4.
 */

public class EditPasswordActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private static final int VALIDATEPASSWORDSUCCESS = 1;
    private static final int VALIDATEPASSWORDFAIL = 2;
    private static final int EDITPASSWORDSUCCESS = 3;
    private static final int EDITPASSWORDFAIL = 4;
    private String walletAddress;
    private Button btn_edit_password;
    private EditText edt_old_password, edt_new_password, edt_re_password;
    private DEFWalletHelper helper;
    private ProgressDialog progressDialog;
    private Toolbar mToolbar;
    private RelativeLayout rl_edit_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case VALIDATEPASSWORDSUCCESS:
                CommonUtils.cancelProgressDialog(progressDialog);
                CommonUtils.buildProgressDialog(this, progressDialog, R.string.editing);
                String result = (String) msg.obj;
                final String oldPassword = result.split("/")[0];
                final String newPassword = result.split("/")[1];
                new Thread() {
                    @Override
                    public void run() {
                        if (helper.modifyPassword(walletAddress, oldPassword, newPassword)) {
                            mHandler.sendEmptyMessage(EDITPASSWORDSUCCESS);
                        } else {
                            mHandler.sendEmptyMessage(EDITPASSWORDFAIL);
                        }
                    }
                }.start();
                break;
            case VALIDATEPASSWORDFAIL:
                CommonUtils.cancelProgressDialog(progressDialog);
                showToast(R.string.password_wrong);
                break;
            case EDITPASSWORDSUCCESS:
                CommonUtils.cancelProgressDialog(progressDialog);
                showToast(R.string.edit_sucess);
                break;
            case EDITPASSWORDFAIL:
                CommonUtils.cancelProgressDialog(progressDialog);
                showToast(R.string.edit_failed);
                break;
        }
    }

    @Override
    protected void initView() {
        helper = DEFWalletHelper.getInstance();
        progressDialog = new ProgressDialog(this);
        walletAddress = getIntent().getStringExtra("walletAddress");

        mToolbar = findViewById(R.id.toolbar_edit_password);
        CommonUtils.buildDefaultToolbar(this, mToolbar);

        btn_edit_password = findViewById(R.id.btn_edit_password);
        edt_old_password = findViewById(R.id.edt_old_password);
        edt_new_password = findViewById(R.id.edt_new_password);
        edt_re_password = findViewById(R.id.edt_re_password);
        rl_edit_password = findViewById(R.id.rl_edit_password);
        btn_edit_password.setEnabled(false);
        btn_edit_password.setOnClickListener(this);
        edt_old_password.addTextChangedListener(this);
        edt_new_password.addTextChangedListener(this);
        edt_re_password.addTextChangedListener(this);
        edt_old_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edt_new_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edt_re_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_edit_password;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_password:
                final String oldPassword = edt_old_password.getText().toString().trim();
                final String newPassword = edt_new_password.getText().toString().trim();
                String reNewPassword = edt_re_password.getText().toString().trim();
                if (newPassword.equals(reNewPassword)) {
                    if (CheckStringUtils.isVerification(newPassword)) {
                        CommonUtils.buildProgressDialog(this, progressDialog, R.string.validate_password);
                        new Thread() {
                            @Override
                            public void run() {
                                if (helper.validatePassword(walletAddress, oldPassword)) {
                                    Message msg = Message.obtain();
                                    msg.what = VALIDATEPASSWORDSUCCESS;
                                    msg.obj = oldPassword + "/" + newPassword;
                                    mHandler.sendMessage(msg);
                                } else {
                                    mHandler.sendEmptyMessage(VALIDATEPASSWORDFAIL);
                                }
                            }
                        }.start();
                    } else {
                        showToast(R.string.password_format_wrong);
                    }
                } else {
                    showToast(R.string.password_different);
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        btn_edit_password.setEnabled(false);
        btn_edit_password.setBackgroundResource(R.drawable.shape_button3);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!edt_old_password.getText().toString().equals("") && !edt_new_password.getText().toString().equals("") && !edt_re_password.getText().toString().equals("")) {
            btn_edit_password.setEnabled(true);
            btn_edit_password.setBackgroundResource(R.drawable.shape_button);
        }
    }
}
