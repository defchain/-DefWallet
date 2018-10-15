package com.lajoin.defwallet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.R;
import com.lajoin.defwallet.adapter.BasePagerAdapter;
import com.lajoin.defwallet.adapter.FragmentAdapter;
import com.lajoin.defwallet.fragment.KeystoreFragment;
import com.lajoin.defwallet.fragment.MnemonicFragment;
import com.lajoin.defwallet.fragment.PrivateKeyFragment;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2018/6/26.
 */

public class ImportWalletActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private AppBarLayout appbar;
    /**
     * 1:startActivity open,2:WalletManagementActivity open;
     */
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wallet);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_import_wallet);
        appbar = findViewById(R.id.appbar);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        initViewPager();
    }

    private void initViewPager() {
        mTabLayout = findViewById(R.id.tl_import_wallet);
        mViewPager = findViewById(R.id.vp_import_wallet);

        List<Integer> titles = new ArrayList<>();
        titles.add(R.string.mnemonic);
        titles.add(R.string.keystore);
        titles.add(R.string.private_key);
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MnemonicFragment());
        fragments.add(new KeystoreFragment());
        fragments.add(new PrivateKeyFragment());

        mViewPager.setOffscreenPageLimit(2);

        FragmentAdapter mFragmentAdapter = new FragmentAdapter(this, getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapter);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return appbar;
    }

}
