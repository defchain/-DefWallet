package com.lajoin.defwallet.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.lajoin.defwallet.adapter.TransactionRecordAdapter;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.entity.TransactionResultEntity;
import com.lajoin.defwallet.ethUtils.ETHWebService;
import com.lajoin.defwallet.ethUtils.ETHWebServiceListener;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.JsonParseUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2018/6/29.
 */

public class AssetsDetailsActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final int GETTRANSACTIONRECORDSUCCESS = 1;
    private static final int GETTRANSACTIONRECORDFAILED = 2;
    private ImageView img_currency_icon, img_back;
    private TextView tv_currency_name, tv_currency_type;
    private TextView tv_send, tv_receive, tv_currency_value, tv_currency_number;
    private RelativeLayout rl_transaction_record;
    private ListView lv_transaction_record;
    private ETHWebService ethWebService;
    private TransactionRecordAdapter adapter;
    private List<TransactionResultEntity> mTransactionResultEntityList;
    private SwipeRefreshLayout srl_assets_details;
    private DigitalEntity mDigitalEntity;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assetsdetails);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case GETTRANSACTIONRECORDSUCCESS:
                String transactionsJson = (String) msg.obj;
                mTransactionResultEntityList.clear();
                try {
                    mTransactionResultEntityList = JsonParseUtils.getTransactionsList(transactionsJson, "0x" + DefApplication.getCurrentUser().getAddress());
                    if (mTransactionResultEntityList.size() > 0) {
                        rl_transaction_record.setVisibility(View.GONE);
                        lv_transaction_record.setVisibility(View.VISIBLE);
                        adapter = new TransactionRecordAdapter(this, mTransactionResultEntityList);
                        lv_transaction_record.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                srl_assets_details.setRefreshing(false);
                break;
            case GETTRANSACTIONRECORDFAILED:
                srl_assets_details.setRefreshing(false);
                break;
        }
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        mDigitalEntity = (DigitalEntity) intent.getSerializableExtra("mDigital");

        ethWebService = ETHWebService.getInstance(this);
        mTransactionResultEntityList = new ArrayList<>();

        img_currency_icon = findViewById(R.id.img_currency_icon);
        tv_currency_name = findViewById(R.id.tv_currency_name);
        img_back = findViewById(R.id.img_back);
        tv_currency_value = findViewById(R.id.tv_currency_value);
        tv_currency_number = findViewById(R.id.tv_currency_number);
        lv_transaction_record = findViewById(R.id.lv_transaction_record);
        lv_transaction_record.setOnItemClickListener(this);
        rl_transaction_record = findViewById(R.id.rl_transaction_record);
        tv_currency_type = findViewById(R.id.tv_currency_type);
        srl_assets_details = findViewById(R.id.srl_assets_details);
        srl_assets_details.setColorSchemeResources(R.color.colorAccent);
        rl_root = findViewById(R.id.rl_root);
        srl_assets_details.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CheckNetWork();
                loadData();
            }
        });

        CommonUtils.setCurrencyShow(tv_currency_type, R.string.rmb_symbol, R.string.usd_symbol);


        if (!TextUtils.isEmpty(mDigitalEntity.getIconPath()) && mDigitalEntity.getCurrencyName() != null && !mDigitalEntity.getCurrencyName().equals("")) {
            ImageLoader.getInstance().displayImage(mDigitalEntity.getIconPath(), img_currency_icon, DefApplication.getOptions());
            tv_currency_name.setText(mDigitalEntity.getCurrencyName());
        }
        tv_send = findViewById(R.id.tv_send);
        tv_receive = findViewById(R.id.tv_receive);
        tv_currency_value.setText(mDigitalEntity.getCurrencyValue());
        tv_currency_number.setText(mDigitalEntity.getHasCurrency());
        tv_send.setOnClickListener(this);
        tv_receive.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send:
                Intent intent = new Intent(this, SendActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("mDigital", mDigitalEntity);
                intent.putExtras(mBundle);
                startActivity(intent);
                break;
            case R.id.tv_receive:
                if (TextUtils.isEmpty(DefApplication.getCurrentUser().getMnemonic())) {
                    startActivity(new Intent(AssetsDetailsActivity.this, ReceivablesActivity.class).putExtra("address", "0x" + DefApplication.getCurrentUser().getAddress()).putExtra("walletName", DefApplication.getCurrentUser().getWalletName()));
                } else {
                    new AlertDialog.Builder(this).setTitle(getString(R.string.no_backup_mnemonic_dialog_title))
                            .setMessage(getString(R.string.no_backup_mnemonic_dialog_msg))
                            .setPositiveButton(getString(R.string.no_backup_mnemonic_dialog_btn_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(AssetsDetailsActivity.this, BackupWarningActivity.class).putExtra("walletAddress", DefApplication.getCurrentUser().getAddress()).putExtra("_type", 0));
                                }
                            })
                            .setNegativeButton(getString(R.string.no_backup_mnemonic_dialog_btn_cancel), null)
                            .show();
                }
                break;
            case R.id.img_back:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        DefApplication.setTransactionResultEntity(mTransactionResultEntityList.get(position));
        startActivity(TransactionSuccessActivity.class);
    }

    private void loadData() {
        srl_assets_details.setRefreshing(true);
        if (mDigitalEntity.getContract().equals(Constants.ETHCONTRACT)) {
            new Thread() {
                @Override
                public void run() {
                    ethWebService.getTransactionsByAddress("0x" + DefApplication.getCurrentUser().getAddress(), 1, 20, new ETHWebServiceListener() {
                        @Override
                        public void success(String result) {
                            Message msg = Message.obtain();
                            msg.what = GETTRANSACTIONRECORDSUCCESS;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void failed() {
                            mHandler.sendEmptyMessage(GETTRANSACTIONRECORDFAILED);
                        }
                    });
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    ethWebService.getERC20Tansaction("0x" + DefApplication.getCurrentUser().getAddress(), mDigitalEntity.getContract(), 1, 20, new ETHWebServiceListener() {
                        @Override
                        public void success(String result) throws JSONException {
                            Message msg = Message.obtain();
                            msg.what = GETTRANSACTIONRECORDSUCCESS;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void failed() {
                            mHandler.sendEmptyMessage(GETTRANSACTIONRECORDFAILED);
                        }
                    });
                }
            }.start();
        }

    }
}
