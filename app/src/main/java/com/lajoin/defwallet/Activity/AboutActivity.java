package com.lajoin.defwallet.Activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.BuildConfig;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;

public class AboutActivity extends BaseActivity {
    private Toolbar mToolbar;
    private TextView tv_version_about;
    private RelativeLayout rl_about_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_about);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        tv_version_about = findViewById(R.id.tv_version_about);
        String stringVersion = String.format(getResources().getString(R.string.version), BuildConfig.VERSION_NAME);
        tv_version_about.setText(stringVersion);
        rl_about_activity = findViewById(R.id.rl_about_activity);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_about_activity;
    }
}
