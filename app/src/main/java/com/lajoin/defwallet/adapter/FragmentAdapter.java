package com.lajoin.defwallet.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private Context context;
    private List<Fragment> mFragments;
    private List<Integer> mTitles;

    public FragmentAdapter(Context context, FragmentManager fm, List<Fragment> fragments, List<Integer> titles) {
        super(fm);
        this.context = context;
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getString(mTitles.get(position));
    }
}
