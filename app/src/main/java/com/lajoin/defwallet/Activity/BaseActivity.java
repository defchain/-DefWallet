package com.lajoin.defwallet.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.broadcastReceiver.NetBroadcastReceiver;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.view.WarningView;


/**
 * Created by hasee on 2018/8/30.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public Handler mHandler;
    private NetBroadcastReceiver netBroadcastReceiver;
    public WarningView mWarningView;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.INTERNETCHANGEED:
                    if (getRootRelativeLayout() != null) {
                        CheckNetWork();
                    }
                    break;
                case Constants.SHOWWARNINGVIEW:
                    if (getRootRelativeLayout() != null) {
                        CommonUtils.cancelWarning(BaseActivity.this, mWarningView, getRootRelativeLayout());
                    }
                    break;
                case Constants.INTERNETISBADSHOW:
                    showToast(R.string.internet_bad);
                    break;
            }
        }
    };

    public void CheckNetWork() {
        if (!CommonUtils.isNetworkAvalible(BaseActivity.this)) {
            mWarningView = new WarningView(BaseActivity.this, R.string.no_internet);
            CommonUtils.buildWarning(BaseActivity.this, getRootRelativeLayout(), mWarningView, handler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getRootRelativeLayout() != null) {
            if (mWarningView != null) {
                if (mWarningView.getVisibility() == View.VISIBLE) {
                    CommonUtils.cancelWarning(BaseActivity.this, mWarningView, getRootRelativeLayout());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.titlebarcolor));
        }
        setHandler();
        initReceiver();
        netBroadcastReceiver = new NetBroadcastReceiver(handler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Constants.INTERNETISBAD);
        registerReceiver(netBroadcastReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netBroadcastReceiver);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
        setData();
    }


    protected void setHandler() {
        if (mHandler != null) {
            return;
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleMsg(msg);
            }
        };
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }


    protected void setActivityBackground(int resId) {
        getWindow().setBackgroundDrawableResource(resId);
    }

    protected void setActivityBackground(Bitmap bitmap) {
        getWindow().setBackgroundDrawable(new BitmapDrawable(bitmap));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(int strId) {
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    protected final void startActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    protected final void startActivity(int flags, Class<?> targetActivity) {
        final Intent intent = new Intent(this, targetActivity);
        intent.setFlags(flags);
        startActivity(new Intent(this, targetActivity));
    }

    protected abstract void handleMsg(Message msg);

    protected abstract void initView();

    protected abstract void setData();

    protected abstract void initReceiver();

    protected abstract ViewGroup getRootRelativeLayout();


}
