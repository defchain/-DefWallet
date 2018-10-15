package com.lajoin.defwallet.Activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;

public class SettingActivity extends BaseActivity {
    private Toolbar mToolbar;
    private CardView card_currency;
    private TextView tv_currency_name;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_setting);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        card_currency = findViewById(R.id.card_currency);
        tv_currency_name = findViewById(R.id.tv_currency_name);
        rl_root = findViewById(R.id.rl_root);

        card_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ChoiceMoneyActivity.class);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        CommonUtils.setCurrencyShow(tv_currency_name, R.string.rmb, R.string.usd);
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

}
