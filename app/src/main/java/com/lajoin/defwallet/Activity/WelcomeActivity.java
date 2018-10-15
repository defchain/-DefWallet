package com.lajoin.defwallet.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaExtractor;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;

import java.io.File;
import java.util.List;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/6/22.
 */

public class WelcomeActivity extends BaseActivity {
    private static final int STARTACTIVITY = 1;
    private static final int STARTMAINACTIVITY = 2;
    private SharedPreferences mSharedPrefernces;
    private String recodeWalletAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.titlebarcolor));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case STARTACTIVITY:
                startActivity(StartActivity.class);
                finish();
                break;
            case STARTMAINACTIVITY:
                String address = (String) msg.obj;
                startActivity(new Intent(this, MainActivity.class).putExtra("walletAddress", address));
                finish();
                break;
            case Constants.GETLISTSUCCESS:
                List<DEFWallet> defWalletList = (List<DEFWallet>) msg.obj;
                boolean flag = false;
                if (defWalletList.size() > 0) {
                    for (int i = 0; i < defWalletList.size(); i++) {
                        if (defWalletList.get(i).getAddress().equals(recodeWalletAddress)) {
                            flag = true;
                            DefApplication.setCurrentUser(defWalletList.get(i));
                            break;
                        } else {
                            flag = false;
                        }
                    }
                    if (!flag) {
                        DefApplication.setCurrentUser(defWalletList.get(0));
                    }
                    Message message = Message.obtain();
                    message.what = STARTMAINACTIVITY;
                    message.obj = defWalletList.get(0).getAddress();
                    mHandler.sendMessageDelayed(message, 2000);
                } else {
                    mHandler.sendEmptyMessageDelayed(STARTACTIVITY, 2000);
                }
                break;
        }
    }

    @Override
    protected void initView() {
        mSharedPrefernces = getSharedPreferences(Constants.WALLET_PREFERENCES_KEY, Context.MODE_PRIVATE);
        recodeWalletAddress = mSharedPrefernces.getString(Constants.WALLET_PREFERENCES_KEY, "");
    }

    @Override
    protected void setData() {
        new Thread() {
            @Override
            public void run() {
                List<DEFWallet> defWalletList = DEFWalletHelper.getInstance().listAllWallets();
                if (defWalletList != null) {
                    Message msg = Message.obtain();
                    msg.what = Constants.GETLISTSUCCESS;
                    msg.obj = defWalletList;
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return null;
    }


}
