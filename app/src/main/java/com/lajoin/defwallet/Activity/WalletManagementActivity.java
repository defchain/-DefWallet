package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.adapter.WalletManagementAdapter;
import com.lajoin.defwallet.callbaceListener.WalletAssetsValueCallbackListener;
import com.lajoin.defwallet.entity.WalletEntity;
import com.lajoin.defwallet.ethUtils.ETHWebService;
import com.lajoin.defwallet.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/6/27.
 */

public class WalletManagementActivity extends BaseActivity implements View.OnClickListener {
    private static final int GETWALLETLIST = 1;
    private static final int GETWALLETASSETS = 2;
    private static final int GETWALLETASSETSFAILED = 4;
    private static final int NOWALLET = 3;
    private TextView tv_management_left, tv_management_right;
    private ListView lv_management;
    private List<WalletEntity> walletEntities = new ArrayList<>();
    private WalletManagementAdapter adapter;
    private RelativeLayout rl_wallet_management;

    private DEFWalletHelper helper;
    private ETHWebService ethWebService;

    private Toolbar mToolbar;

    private List<DEFWallet> defWalletList;
    private SwipeRefreshLayout srl_wallet_management;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_management);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case GETWALLETLIST:
                defWalletList = (List<DEFWallet>) msg.obj;
                walletEntities.clear();
                for (int i = 0; i < defWalletList.size(); i++) {
                    final WalletEntity walletEntity = new WalletEntity();
                    walletEntity.setWalletName(defWalletList.get(i).getWalletName());
                    walletEntity.setWalletAddress("0x" + defWalletList.get(i).getAddress());
                    final int finalI = i;
                    CommonUtils.getWalletAssetsValue(ethWebService, "0x" + defWalletList.get(i).getAddress(), new WalletAssetsValueCallbackListener() {
                        @Override
                        public void getAssetsValue(String assets) {
                            double doubleUsd = Double.parseDouble(assets);
                            if (TextUtils.isEmpty(DefApplication.getCurrencyType()) || DefApplication.getCurrencyType().equals(Constants.RMB)) {
                                if (DefApplication.getUsdToRmbRate() != 0) {
                                    double doubleRmb = doubleUsd * DefApplication.getUsdToRmbRate();
                                    walletEntity.setWalletValue(CommonUtils.showDouble(doubleRmb));
                                    Message message = Message.obtain();
                                    message.what = GETWALLETASSETS;
                                    message.obj = finalI;
                                    mHandler.sendMessage(message);
                                } else {

                                }
                            } else if (DefApplication.getCurrencyType().equals(Constants.USD)) {
                                walletEntity.setWalletValue(CommonUtils.showDouble(doubleUsd));
                                Message message = Message.obtain();
                                message.what = GETWALLETASSETS;
                                message.obj = finalI;
                                mHandler.sendMessage(message);
                            }
                        }

                        @Override
                        public void getAssetsFailed() {
                            mHandler.sendEmptyMessage(GETWALLETASSETSFAILED);
                        }
                    });
                    if (TextUtils.isEmpty(DefApplication.getCurrencyType()) || DefApplication.getCurrencyType().equals(Constants.RMB)) {
                        walletEntity.setWalletCurrencyType(getResources().getString(R.string.rmb_symbol));
                    } else if (DefApplication.getCurrencyType().equals(Constants.USD)) {
                        walletEntity.setWalletCurrencyType(getResources().getString(R.string.usd_symbol));
                    }
                    walletEntities.add(walletEntity);
                }
                adapter.notifyDataSetChanged();
                break;
            case GETWALLETASSETS:
                int num = (int) msg.obj;
                adapter.notifyDataSetChanged();
                srl_wallet_management.setRefreshing(false);
                break;
            case NOWALLET:
                walletEntities.clear();
                adapter.notifyDataSetChanged();
                srl_wallet_management.setRefreshing(false);
                break;
            case GETWALLETASSETSFAILED:
                srl_wallet_management.setRefreshing(false);
                break;
        }
    }

    @Override
    protected void initView() {
        helper = DEFWalletHelper.getInstance();
        ethWebService = ETHWebService.getInstance(this);
        mToolbar = findViewById(R.id.toolbar_wallet_management);
        mToolbar.setNavigationIcon(R.mipmap.arrow_left);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (walletEntities.size() < 1) {
                    sendBroadcast(new Intent(Constants.FINISHMAINACTIVITY));
                    startActivity(StartActivity.class);
                    finish();
                } else {
                    finish();
                }
            }
        });
        tv_management_left = findViewById(R.id.tv_management_left);
        rl_wallet_management = findViewById(R.id.rl_wallet_management);
        tv_management_right = findViewById(R.id.tv_management_right);
        tv_management_left.setOnClickListener(this);
        tv_management_right.setOnClickListener(this);
        lv_management = findViewById(R.id.lv_management);
        srl_wallet_management = findViewById(R.id.srl_wallet_management);
        srl_wallet_management.setColorSchemeResources(R.color.colorAccent);

        adapter = new WalletManagementAdapter(this, walletEntities);
        lv_management.setAdapter(adapter);
        lv_management.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(WalletManagementActivity.this, WalletDetailsActivity.class).putExtra("walletName", adapter.getItem(position).getWalletName()).putExtra("walletAddress", adapter.getItem(position).getWalletAddress()).putExtra("walletValue", adapter.getItem(position).getWalletValue()));
            }
        });
        srl_wallet_management.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CheckNetWork();
                setData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    @Override
    protected void setData() {
        srl_wallet_management.setRefreshing(true);
        new Thread() {
            @Override
            public void run() {
                List<DEFWallet> defWalletList = helper.listAllWallets();
                if (defWalletList.size() > 0) {
                    Message msg = Message.obtain();
                    msg.what = GETWALLETLIST;
                    msg.obj = defWalletList;
                    mHandler.sendMessage(msg);
                } else {
                    mHandler.sendEmptyMessage(NOWALLET);
                }
            }
        }.start();
    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_wallet_management;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_management_left:
                startActivity(CreatWalletActivity.class);
                break;
            case R.id.tv_management_right:
                startActivity(new Intent(this, ImportWalletActivity.class).putExtra("_type", 2));
                break;

        }
    }
}
