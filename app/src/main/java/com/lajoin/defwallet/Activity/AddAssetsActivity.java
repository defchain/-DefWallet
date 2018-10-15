package com.lajoin.defwallet.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.adapter.AddAssetsAdapter;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.JsonParseUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.utils.WalletAssetsSharedPreferencesUtils;
import com.lajoin.defwallet.utils.WalletRequestCallbackListener;
import com.lajoin.defwallet.utils.WalletRequestUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AddAssetsActivity extends BaseActivity implements TextWatcher {
    private static final int GETALLDIGITAL = 1;
    private static final int GETALLDIGITALFAILED = 2;
    private static final int SEARCHRESULT = 3;
    private Toolbar mToolbar;
    private EditText edt_search_assets;
    private ListView lv_add_assets;
    private AddAssetsAdapter addAssetsAdapter;
    private List<DigitalEntity> allDigitalEntityList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private List<DigitalEntity> mDigitalEntityList;
    private RelativeLayout rl_root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assets);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case GETALLDIGITAL:
                allDigitalEntityList.clear();
                allDigitalEntityList.addAll((List<DigitalEntity>) msg.obj);
                addAssetsAdapter.notifyDataSetChanged();
                CommonUtils.cancelProgressDialog(progressDialog);
                break;
            case GETALLDIGITALFAILED:
                break;
            case SEARCHRESULT:
                String result = (String) msg.obj;
                try {
                    allDigitalEntityList.clear();
                    allDigitalEntityList.addAll(JsonParseUtils.getAllDigitalList(result));
                    addAssetsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void initView() {
        progressDialog = new ProgressDialog(this);
        CommonUtils.buildProgressDialog(this, progressDialog, R.string.please_wait);
        mToolbar = findViewById(R.id.toolbar_add_assets);
        edt_search_assets = findViewById(R.id.edt_search_assets);
        lv_add_assets = findViewById(R.id.lv_add_assets);
        rl_root = findViewById(R.id.rl_root);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        edt_search_assets.addTextChangedListener(this);
        mDigitalEntityList = WalletAssetsSharedPreferencesUtils.getInstance(this).getDigitaEntityBywalletAddress(DefApplication.getCurrentUser().getAddress());
        addAssetsAdapter = new AddAssetsAdapter(this, allDigitalEntityList, mDigitalEntityList);
        lv_add_assets.setAdapter(addAssetsAdapter);
    }

    @Override
    protected void setData() {
        new Thread() {
            @Override
            public void run() {
                WalletRequestUtils.getInstance(AddAssetsActivity.this).getAllDigital(new WalletRequestCallbackListener() {
                    @Override
                    public void success(String result) {
                        try {
                            List<DigitalEntity> list = JsonParseUtils.getAllDigitalList(result);
                            Message msg = Message.obtain();
                            msg.what = GETALLDIGITAL;
                            msg.obj = list;
                            mHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed() {
                        mHandler.sendEmptyMessage(GETALLDIGITALFAILED);
                    }
                });
            }
        }.start();
    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_root;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String content = edt_search_assets.getText().toString().trim();
        if (content.contains("0x")) {
            new Thread() {
                @Override
                public void run() {
                    WalletRequestUtils.getInstance(AddAssetsActivity.this).inquireDigitalByContract(content, new WalletRequestCallbackListener() {
                        @Override
                        public void success(String result) throws JSONException {
                            Message msg = Message.obtain();
                            msg.what = SEARCHRESULT;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void failed() {

                        }
                    });
                }
            }.start();

        } else if (content.equals("")) {
            new Thread() {
                @Override
                public void run() {
                    WalletRequestUtils.getInstance(AddAssetsActivity.this).getAllDigital(new WalletRequestCallbackListener() {
                        @Override
                        public void success(String result) {
                            try {
                                List<DigitalEntity> list = JsonParseUtils.getAllDigitalList(result);
                                Message msg = Message.obtain();
                                msg.what = GETALLDIGITAL;
                                msg.obj = list;
                                mHandler.sendMessage(msg);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed() {
                            mHandler.sendEmptyMessage(GETALLDIGITALFAILED);
                        }
                    });
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    WalletRequestUtils.getInstance(AddAssetsActivity.this).inquireDigitalByName(content, new WalletRequestCallbackListener() {
                        @Override
                        public void success(String result) throws JSONException {
                            Message msg = Message.obtain();
                            msg.what = SEARCHRESULT;
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
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


}
