package com.lajoin.defwallet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by xgp on 2017/12/18.
 */

public class BasePagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public BasePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = fragments.get(0);
                break;
            case 1:
                fragment = fragments.get(1);
                break;
            case 2:
                fragment = fragments.get(2);
                break;
            case 3:
                fragment = fragments.get(3);
                break;
            case 4:
                fragment = fragments.get(4);
                break;
            case 5:
                fragment = fragments.get(5);
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
