package com.lajoin.defwallet.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;


public class ChoiceMoneyActivity extends BaseActivity implements View.OnClickListener {
    public static final String MONEYTYPE_KEY = "money_key";
    private SharedPreferences mSharedPrefernces;
    private SharedPreferences.Editor mSharedPreferncesEditor;
    private Toolbar mToolbar;
    private TextView tv_save;
    private RadioButton rb_rmb, rb_usd;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_money);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_choice_money);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        tv_save = findViewById(R.id.tv_save);
        rb_rmb = findViewById(R.id.rb_rmb);
        rb_usd = findViewById(R.id.rb_usd);
        rl_root = findViewById(R.id.rl_root);
        if (TextUtils.isEmpty(DefApplication.getCurrencyType()) || DefApplication.getCurrencyType().equals(Constants.RMB)) {
            rb_rmb.setChecked(true);
            rb_usd.setChecked(false);
            DefApplication.setCurrencyType(Constants.RMB);
        } else if (DefApplication.getCurrencyType().equals(Constants.USD)) {
            rb_rmb.setChecked(false);
            rb_usd.setChecked(true);
        }
        mSharedPrefernces = this.getSharedPreferences(MONEYTYPE_KEY, Context.MODE_PRIVATE);
        mSharedPreferncesEditor = mSharedPrefernces.edit();
        mSharedPreferncesEditor.apply();
        tv_save.setOnClickListener(this);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                if (rb_usd.isChecked()) {
                    mSharedPreferncesEditor.putString(MONEYTYPE_KEY, Constants.USD);
                    mSharedPreferncesEditor.commit();
                    DefApplication.setCurrencyType(Constants.USD);
                    finish();
                } else if (rb_rmb.isChecked()) {
                    mSharedPreferncesEditor.putString(MONEYTYPE_KEY, Constants.RMB);
                    mSharedPreferncesEditor.commit();
                    DefApplication.setCurrencyType(Constants.RMB);
                    finish();
                }
                break;
        }
    }
}
