package com.lajoin.defwallet.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.widget.Toast;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.R;


public class NetBroadcastReceiver extends BroadcastReceiver {
    private Handler handler;

    public NetBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            handler.sendEmptyMessage(Constants.INTERNETCHANGEED);
        } else if (intent.getAction().equals(Constants.INTERNETISBAD)) {
            handler.sendEmptyMessage(Constants.INTERNETISBADSHOW);
        }
    }
}
