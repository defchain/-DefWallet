package com.lajoin.defwallet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.adapter.MnemonicAdapter;
import com.lajoin.defwallet.entity.MnemonicButtonEntity;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.view.WarningView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/6/28.
 */

public class ConfirmMnemonicActivity extends BaseActivity {
    private static final int GETMNEMONIC = 1;
    private static final int VERIFICATIONMNEMONICFAILED = 2;
    private static final int DELETEWALLETSUCCESS = 3;
    private static final int DELETEWALLETFAILED = 4;
    private GridView gv_show, gv_button;
    private List<MnemonicButtonEntity> mnemonicButtonEntities = new ArrayList<>();
    private List<MnemonicButtonEntity> mnemonicButtonEntities1 = new ArrayList<>();
    private List<String> stringList = new ArrayList<>();
    private MnemonicAdapter adapter;
    private MnemonicAdapter adapter1;
    private String walletAddress;
    private DEFWalletHelper helper;
    private DEFWallet defWallet;
    private Button btn_confirm;
    private WarningView warningView;

    /**
     * 1:startActivity open,2:Other open;
     */
    private int type;
    private Toolbar mToolbar;
    private RelativeLayout rl_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mnemonic);
        DefApplication.getCreateWalletActivityList().add(this);
        type = getIntent().getIntExtra("_type", -1);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case GETMNEMONIC:
                defWallet = (DEFWallet) msg.obj;
                String[] mnemonics = defWallet.getMnemonic().split(" ");
                if (mnemonics.length > 0) {
                    for (int i = 0; i < mnemonics.length; i++) {
                        stringList.add(mnemonics[i]);
                        LogUtils.log(mnemonics[i] + i);
                    }
                }
                Collections.shuffle(stringList);
                for (int j = 0; j < stringList.size(); j++) {
                    MnemonicButtonEntity mnemonicButtonEntity = new MnemonicButtonEntity();
                    mnemonicButtonEntity.setTextConten(stringList.get(j));
                    mnemonicButtonEntity.setType(1);
                    mnemonicButtonEntity.setPosition(j);
                    mnemonicButtonEntities.add(mnemonicButtonEntity);
                }

                adapter = new MnemonicAdapter(this, mnemonicButtonEntities);
                adapter1 = new MnemonicAdapter(this, mnemonicButtonEntities1);
                gv_button.setAdapter(adapter);
                gv_show.setAdapter(adapter1);
                break;
            case DELETEWALLETSUCCESS:
                if (DefApplication.getCreateWalletActivityList().size() > 0) {
                    for (int i = 0; i < DefApplication.getCreateWalletActivityList().size(); i++) {
                        DefApplication.getCreateWalletActivityList().get(i).finish();
                    }
                    DefApplication.getCreateWalletActivityList().clear();
                }

                new Thread() {
                    @Override
                    public void run() {
                        defWallet = helper.findWalletByAddress(walletAddress);
                        if (defWallet != null) {
                            if (type == 1) {
                                DefApplication.setCurrentUser(defWallet);
                                startActivity(new Intent(ConfirmMnemonicActivity.this, MainActivity.class).putExtra("walletAddress", walletAddress));
                            } else {
                                DefApplication.setCurrentUser(defWallet);
                            }
                        }
                    }
                }.start();
                break;
            case DELETEWALLETFAILED:
                break;
            case VERIFICATIONMNEMONICFAILED:
                warningView = new WarningView(this, R.string.confirm_mnemonic_failed);
                CommonUtils.buildWarning(this, rl_confirm, warningView, mHandler);
                break;
            case Constants.SHOWWARNINGVIEW:
                CommonUtils.cancelWarning(this, warningView, rl_confirm);
                break;
        }
    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_confirm);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        helper = DEFWalletHelper.getInstance();
        Intent intent = getIntent();
        walletAddress = intent.getStringExtra("walletAddress");
        if (walletAddress != null && !walletAddress.equals("")) {
            new Thread() {
                @Override
                public void run() {
                    DEFWallet defWallet = helper.findWalletByAddress(walletAddress);
                    if (defWallet != null) {
                        Message msg = Message.obtain();
                        msg.what = GETMNEMONIC;
                        msg.obj = defWallet;
                        mHandler.sendMessage(msg);
                    }
                }
            }.start();
        }
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setEnabled(false);
        gv_show = findViewById(R.id.gv_show_mnemonic);
        gv_button = findViewById(R.id.gv_mnemonic_button);
        rl_confirm = findViewById(R.id.rl_confirm);

        gv_button.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean flag = false;
                for (int i = 0; i < mnemonicButtonEntities1.size(); i++) {
                    if (mnemonicButtonEntities1.get(i).getTextConten().equals(mnemonicButtonEntities.get(position).getTextConten()) && mnemonicButtonEntities1.get(i).getPosition() == mnemonicButtonEntities.get(position).getPosition()) {
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }
                }
                if (flag) {
                    for (int i = 0; i < mnemonicButtonEntities1.size(); i++) {
                        if (mnemonicButtonEntities1.get(i).getTextConten().equals(mnemonicButtonEntities.get(position).getTextConten()) && mnemonicButtonEntities1.get(i).getPosition() == mnemonicButtonEntities.get(position).getPosition()) {
                            mnemonicButtonEntities.get(position).setType(1);
                            mnemonicButtonEntities1.remove(i);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    adapter1.notifyDataSetChanged();
                    if (mnemonicButtonEntities1.size() == 12) {
                        btn_confirm.setEnabled(true);
                        btn_confirm.setBackgroundResource(R.drawable.shape_button);
                    } else {
                        btn_confirm.setEnabled(false);
                        btn_confirm.setBackgroundResource(R.drawable.shape_button3);
                    }
                } else {
                    mnemonicButtonEntities.get(position).setType(2);
                    adapter.notifyDataSetChanged();
                    MnemonicButtonEntity mnemonicButtonEntity = new MnemonicButtonEntity();
                    mnemonicButtonEntity.setTextConten(mnemonicButtonEntities.get(position).getTextConten());
                    mnemonicButtonEntity.setPosition(mnemonicButtonEntities.get(position).getPosition());
                    mnemonicButtonEntity.setType(3);
                    mnemonicButtonEntities1.add(mnemonicButtonEntity);
                    adapter1.notifyDataSetChanged();
                    if (mnemonicButtonEntities1.size() == 12) {
                        btn_confirm.setEnabled(true);
                        btn_confirm.setBackgroundResource(R.drawable.shape_button);
                    } else {
                        btn_confirm.setEnabled(false);
                        btn_confirm.setBackgroundResource(R.drawable.shape_button3);
                    }
                }
            }
        });

        gv_show.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < mnemonicButtonEntities.size(); i++) {
                    if (mnemonicButtonEntities.get(i).getTextConten().equals(mnemonicButtonEntities1.get(position).getTextConten())) {
                        mnemonicButtonEntities.get(i).setType(1);
                    }
                }
                adapter.notifyDataSetChanged();
                mnemonicButtonEntities1.remove(position);
                adapter1.notifyDataSetChanged();
                if (mnemonicButtonEntities1.size() == 12) {
                    btn_confirm.setEnabled(true);
                    btn_confirm.setBackgroundResource(R.drawable.shape_button);
                } else {
                    btn_confirm.setEnabled(false);
                    btn_confirm.setBackgroundResource(R.drawable.shape_button3);
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mnemonicButtonEntities1.size() != 12) {
                    return;
                }
                String mnemonic = "";
                for (int i = 0; i < mnemonicButtonEntities1.size(); i++) {
                    mnemonic = mnemonic + mnemonicButtonEntities1.get(i).getTextConten() + " ";
                }
                if (defWallet.getMnemonic().equals(mnemonic.trim())) {
                    new Thread() {
                        @Override
                        public void run() {
                            helper.removeWalletMnemonic(defWallet.getAddress());
                            mHandler.sendEmptyMessageDelayed(DELETEWALLETSUCCESS, 500);
                        }
                    }.start();
                } else {
                    //验证失败
                    mHandler.sendEmptyMessage(VERIFICATIONMNEMONICFAILED);
                }
            }
        });
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_confirm;
    }
}
