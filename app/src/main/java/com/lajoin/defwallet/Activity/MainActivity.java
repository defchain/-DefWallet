package com.lajoin.defwallet.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.BuildConfig;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.broadcastReceiver.NetBroadcastReceiver;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.entity.UpdateSelfEntity;
import com.lajoin.defwallet.fragment.AssetsFragment;
import com.lajoin.defwallet.fragment.MainFragment;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.utils.JsonParseUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.utils.UpdateUtils;
import com.lajoin.defwallet.utils.WalletRequestCallbackListener;
import com.lajoin.defwallet.utils.WalletRequestUtils;
import com.lajoin.defwallet.view.UpdateDialog;
import com.lajoin.defwallet.view.WarningView;

import org.json.JSONException;

import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final int EXIT = 0;

    private RelativeLayout bottom_assets, bottom_main, bottom_quotation;
    private ImageView img_assets, img_main, img_quotation;
    private FragmentManager fragmentManager;
    private Fragment assetsFragment, mainFragment;
    private FragmentTransaction transaction;
    private UpdateDialog updateDialog;
    private Locale locale;
    private MyBroadcastReceiver myBroadcastReceiver;
    private boolean isExit = false;
    private SharedPreferences mSharedPrefernces;
    private SharedPreferences.Editor mSharedPreferncesEditor;
    private RelativeLayout activity_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case Constants.HASNEWVERSION:
                String result = (String) msg.obj;
                try {
                    final UpdateSelfEntity updateSelfEntity = JsonParseUtils.getUpdateSelf(result);
                    if (updateSelfEntity != null) {

                        if (updateSelfEntity.getVersionCode() > BuildConfig.VERSION_CODE) {
                            CommonUtils.showDialog(updateDialog);
                            if (updateDialog.getDesc() != null) {
                                if (locale.getLanguage().equals("en")) {
                                    updateDialog.getDesc().setText(updateSelfEntity.getUpdateRemarkEN());
                                } else {
                                    updateDialog.getDesc().setText(updateSelfEntity.getUpdateRemarkCn());
                                }
                            }
                            if (updateDialog.getTv_cancel() != null) {
                                updateDialog.getTv_cancel().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        updateDialog.dismiss();
                                    }
                                });
                            }
                            if (updateDialog.getTv_ok() != null) {
                                updateDialog.getTv_ok().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (CommonUtils.isWifi(MainActivity.this)) {
                                            UpdateUtils.getInstance(MainActivity.this).starDownload(updateSelfEntity.getPackageUrl(), updateSelfEntity.getPackageName());
                                            updateDialog.dismiss();
                                        } else {
                                            new android.app.AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.wifi_title))
                                                    .setMessage(getString(R.string.wifi_desc))
                                                    .setPositiveButton(getString(R.string.wifi_ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            UpdateUtils.getInstance(MainActivity.this).starDownload(updateSelfEntity.getPackageUrl(), updateSelfEntity.getPackageName());
                                                        }
                                                    })
                                                    .setNegativeButton(getString(R.string.wifi_cancel), null)
                                                    .show();
                                            updateDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case EXIT:
                isExit = false;
                break;

        }
    }

    @Override
    protected void initView() {
        fragmentManager = this.getSupportFragmentManager();
        updateDialog = new UpdateDialog(this);
        locale = Locale.getDefault();
        bottom_assets = findViewById(R.id.bottom_assets);
        bottom_main = findViewById(R.id.bottom_main);
        bottom_quotation = findViewById(R.id.bottom_quotation);
        bottom_assets.setOnClickListener(this);
        bottom_main.setOnClickListener(this);
        bottom_quotation.setOnClickListener(this);
        img_assets = findViewById(R.id.img_bottom_assets);
        img_quotation = findViewById(R.id.img_bottom_quotation);
        img_main = findViewById(R.id.img_bottom_main);
        activity_main = findViewById(R.id.activity_main);
        assetsFragment = new AssetsFragment();
        mainFragment = new MainFragment();
        transaction = fragmentManager.beginTransaction();
        if (!assetsFragment.isAdded()) {
            transaction.add(R.id.main_fragment, assetsFragment);
            transaction.commit();
        }

        mSharedPrefernces = getSharedPreferences(Constants.WALLET_PREFERENCES_KEY, Context.MODE_PRIVATE);
        mSharedPreferncesEditor = mSharedPrefernces.edit();
        mSharedPreferncesEditor.apply();
    }

    @Override
    protected void setData() {
        new Thread() {
            @Override
            public void run() {
                WalletRequestUtils.getInstance(MainActivity.this).getWalletLatestVersion(new WalletRequestCallbackListener() {
                    @Override
                    public void success(String result) {
                        Message msg = Message.obtain();
                        msg.what = Constants.HASNEWVERSION;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void failed() {

                    }
                });
            }
        }.start();
    }

    @Override
    protected void initReceiver() {
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.FINISHMAINACTIVITY);
        registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return activity_main;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_assets:
                setFragment(assetsFragment);
                setBottomImg(R.id.img_bottom_assets);
                break;
            case R.id.bottom_quotation:
                showToast(R.string.function_unopened);
                break;
            case R.id.bottom_main:
                setFragment(mainFragment);
                setBottomImg(R.id.img_bottom_main);
                break;
        }
    }

    private void setFragment(Fragment fragment) {
        transaction = fragmentManager.beginTransaction();
        if (fragment instanceof AssetsFragment) {
            transaction.remove(mainFragment);
        } else if (fragment instanceof MainFragment) {
            transaction.remove(assetsFragment);
        }
        if (fragment.isAdded()) {
        } else {
            transaction.add(R.id.main_fragment, fragment);
            transaction.commit();
        }
    }

    private void setBottomImg(int id) {
        if (id == R.id.img_bottom_assets) {
            img_assets.setImageResource(R.drawable.assets_focus);
            img_quotation.setImageResource(R.drawable.quotation_normal);
            img_main.setImageResource(R.drawable.main_normal);
        } else if (id == R.id.img_bottom_quotation) {
            img_assets.setImageResource(R.drawable.assets_normal);
            img_quotation.setImageResource(R.drawable.quotation_focus);
            img_main.setImageResource(R.drawable.main_normal);
        } else if (id == R.id.img_bottom_main) {
            img_assets.setImageResource(R.drawable.assets_normal);
            img_quotation.setImageResource(R.drawable.quotation_normal);
            img_main.setImageResource(R.drawable.main_focus);
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.FINISHMAINACTIVITY)) {
                finish();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CommonUtils.CAMERA_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendBroadcast(new Intent(Constants.PERMISSIONS_SUCCESS));
            } else {
                sendBroadcast(new Intent(Constants.PERMISSIONS_FAIL));
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            showToast(R.string.exit);
            mHandler.sendEmptyMessageDelayed(EXIT, 2000);
        } else {
            mSharedPreferncesEditor.putString(Constants.WALLET_PREFERENCES_KEY, DefApplication.getCurrentUser().getAddress());
            mSharedPreferncesEditor.commit();
            finish();
            System.exit(0);
        }
    }

}
