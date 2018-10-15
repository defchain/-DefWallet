package com.lajoin.defwallet.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.DigitalEntity;
import com.lajoin.defwallet.entity.TransactionResultEntity;
import com.lajoin.defwallet.ethUtils.ETHWebService;
import com.lajoin.defwallet.ethUtils.ETHWebServiceListener;
import com.lajoin.defwallet.utils.CheckStringUtils;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.utils.JsonParseUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.view.AuthenticationDialog;
import com.lajoin.defwallet.view.WarningView;

import org.json.JSONException;


import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/7/2.
 */

public class SendActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, TextWatcher {
    private static final int PASSWORDSUCCESS = 1;
    private static final int PASSWORDFAILED = 2;
    private static final int SIGNTRANSACTIONSUCCESS = 5;
    private static final int SIGNTRANSACTIONFAILED = 6;
    private static final int GETNONCESUCCESS = 7;
    private static final int GETNONCFAILED = 8;
    private static final int SENDTRANSACTIONSUCCESS = 9;
    private static final int SENDTRANSACTIONFAILED = 10;
    private static final int GETGASPRICESUCCESS = 11;
    private static final int GETGASPRICEFAILED = 12;
    private SeekBar seekbar;
    private TextView tv_mining_fee_value;
    private ImageView iv_send_address;
    private EditText edt_send_address, edt_send_amount, edt_memo;
    private Button btn_send;
    private MBroadcastReceiver mBroadcastReceiver;
    private AuthenticationDialog authenticationDialog;
    private EditText passwordEditText;
    private DEFWalletHelper helper;
    private ProgressDialog progressDialog;
    private String password;
    private long gasLimit;
    private double gasPrice;
    private double minGasPrice;
    private long nonce;
    private boolean hasNonce;
    private ETHWebService ethWebService;
    private TransactionResultEntity transactionRecodeEntity;
    private Toolbar mToolbar;
    private WarningView warningView;
    private DigitalEntity mDigitalEntity;
    private RelativeLayout rl_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case PASSWORDSUCCESS:
                passwordEditText.setText("");
                CommonUtils.buildProgressDialog(this, progressDialog, R.string.signing);
                final String to = edt_send_address.getText().toString().trim();
                final double value = Double.parseDouble(edt_send_amount.getText().toString().trim());
                transactionRecodeEntity.setToAddress(to);
                String memo = edt_memo.getText().toString().trim();
                transactionRecodeEntity.setMemo(memo);
                transactionRecodeEntity.setValue(CommonUtils.showDouble(value) + mDigitalEntity.getCurrencyName());
                if (mDigitalEntity.getContract().equals("0x")) {
                    new Thread() {
                        @Override
                        public void run() {
                            String sign = helper.signTransaction("0x" + DefApplication.getCurrentUser().getAddress(), password, to, value, gasLimit, gasPrice, nonce);
                            if (!sign.equals("")) {
                                Message msg = Message.obtain();
                                msg.what = SIGNTRANSACTIONSUCCESS;
                                msg.obj = sign;
                                mHandler.sendMessage(msg);
                            } else {
                                mHandler.sendEmptyMessage(SIGNTRANSACTIONFAILED);
                            }
                        }
                    }.start();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            long tokenSymbol = Long.parseLong(mDigitalEntity.getDecimal_num());
                            String sign = helper.signERC20Transaction("0x" + DefApplication.getCurrentUser().getAddress(), password, to, mDigitalEntity.getContract(), value, tokenSymbol, gasLimit, gasPrice, nonce);
                            if (!sign.equals("")) {
                                Message msg = Message.obtain();
                                msg.what = SIGNTRANSACTIONSUCCESS;
                                msg.obj = sign;
                                mHandler.sendMessage(msg);
                            } else {
                                mHandler.sendEmptyMessage(SIGNTRANSACTIONFAILED);
                            }
                        }
                    }.start();

                }

                break;
            case PASSWORDFAILED:
                warningView = new WarningView(SendActivity.this, getResources().getString(R.string.password_wrong));
                CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                break;
            case Constants.SHOWWARNINGVIEW:
                CommonUtils.cancelWarning(this, warningView, rl_send);
                break;
            case GETNONCESUCCESS:
                String nonceJson = (String) msg.obj;
                try {
                    nonce = JsonParseUtils.parseNonce(nonceJson);
                    hasNonce = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case GETNONCFAILED:
                break;
            case SIGNTRANSACTIONSUCCESS:
                CommonUtils.cancelProgressDialog(progressDialog);
                btn_send.setEnabled(false);
                btn_send.setBackgroundResource(R.drawable.shape_button3);
                final String sign = (String) msg.obj;
                new Thread() {
                    @Override
                    public void run() {
                        ethWebService.sendRawTransaction(sign, new ETHWebServiceListener() {
                            @Override
                            public void success(String result) {
                                Message msg = Message.obtain();
                                msg.what = SENDTRANSACTIONSUCCESS;
                                msg.obj = result;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void failed() {
                                mHandler.sendEmptyMessage(SENDTRANSACTIONFAILED);
                            }
                        });
                    }
                }.start();
                break;
            case SIGNTRANSACTIONFAILED:
                break;
            case SENDTRANSACTIONSUCCESS:
                String sendJson = (String) msg.obj;
                String time = CommonUtils.formatTime(System.currentTimeMillis());
                transactionRecodeEntity.setTime(time);
                try {
                    String result = JsonParseUtils.getTransactionResult(sendJson);
                    if (!result.isEmpty()) {
                        String flag = result.split(Constants.MYSPLICESYMBOL)[0];
                        if (flag.equals(Constants.RESULT_SUCCESS)) {
                            String order = result.split(Constants.MYSPLICESYMBOL)[1];
                            transactionRecodeEntity.setOrder(order);
                            transactionRecodeEntity.setUrl(CommonUtils.buildTransactionUrl(order));
                            DefApplication.setTransactionResultEntity(transactionRecodeEntity);
                            startActivity(TransactionSuccessActivity.class);
                            finish();
                        } else if (flag.equals(Constants.RESULT_ERROR)) {
                            String error = result.split(Constants.MYSPLICESYMBOL)[1];
                            if (error.equals("insufficient funds for gas * price + value")) {
                                warningView = new WarningView(SendActivity.this, getResources().getString(R.string.send_error));
                                CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                            } else {
                                warningView = new WarningView(SendActivity.this, error);
                                CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                            }
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SENDTRANSACTIONFAILED:
                break;
            case GETGASPRICESUCCESS:
                String gasP = (String) msg.obj;
                long gasPl = Long.parseLong(gasP.substring(2, gasP.length()), 16);
                LogUtils.log("gaspl=" + gasPl);
                double nowGasPrice = gasPl / Math.pow(10, 9);
                LogUtils.log("nowGasPrice=" + nowGasPrice);
                minGasPrice = getMinGasPrice(nowGasPrice);
                tv_mining_fee_value.setText(CommonUtils.showDouble(getGasFee(minGasPrice)) + " eth");
                gasPrice = minGasPrice;
                LogUtils.log("gasPrice=" + gasPrice);
                seekbar.setEnabled(true);
                break;
            case GETGASPRICEFAILED:
                break;
        }
    }

    @Override
    protected void initView() {
        //actionbar
        mToolbar = findViewById(R.id.toolbar_send);
        CommonUtils.buildDefaultToolbar(this, mToolbar);

        mDigitalEntity = (DigitalEntity) getIntent().getSerializableExtra("mDigital");
        String toAddress = getIntent().getStringExtra("toAddress");

        gasLimit = mDigitalEntity.getGasLimit();
        LogUtils.log("gasLimit=" + gasLimit);

        authenticationDialog = new AuthenticationDialog(this);
        helper = DEFWalletHelper.getInstance();
        progressDialog = new ProgressDialog(this);
        ethWebService = ETHWebService.getInstance(this);

        transactionRecodeEntity = new TransactionResultEntity();

        seekbar = findViewById(R.id.seekbar);
        tv_mining_fee_value = findViewById(R.id.tv_mining_fee_value);
        iv_send_address = findViewById(R.id.iv_send_address);
        edt_send_address = findViewById(R.id.edt_send_address);
        edt_send_amount = findViewById(R.id.edt_send_amount);
        edt_memo = findViewById(R.id.edt_memo);
        btn_send = findViewById(R.id.btn_send);
        btn_send.setEnabled(false);
        btn_send.setBackgroundResource(R.drawable.shape_button3);
        rl_send = findViewById(R.id.rl_send);

        if (toAddress != null && !TextUtils.isEmpty(toAddress)) {
            edt_send_address.setText(toAddress);
        }


        iv_send_address.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        edt_send_address.addTextChangedListener(this);
        edt_send_amount.addTextChangedListener(this);
        seekbar.setMax(100);
        seekbar.setEnabled(false);

        hasNonce = false;
        new Thread() {
            @Override
            public void run() {
                ethWebService.getTransactionCount(DefApplication.getCurrentUser().getAddress(), new ETHWebServiceListener() {
                    @Override
                    public void success(String result) {
                        Message msg = Message.obtain();
                        msg.what = GETNONCESUCCESS;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void failed() {
                        mHandler.sendEmptyMessage(GETNONCFAILED);
                    }
                });
            }
        }.start();
    }

    @Override
    protected void setData() {
        new Thread() {
            @Override
            public void run() {
                ETHWebService.getInstance(SendActivity.this).getGasPrice(new ETHWebServiceListener() {
                    @Override
                    public void success(String result) {
                        try {
                            String gasPrice = JsonParseUtils.getGasPrice(result);
                            Message msg = Message.obtain();
                            msg.what = GETGASPRICESUCCESS;
                            msg.obj = gasPrice;
                            mHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed() {
                        mHandler.sendEmptyMessage(GETGASPRICEFAILED);
                    }
                });
            }
        }.start();
    }

    @Override
    protected void initReceiver() {
        mBroadcastReceiver = new MBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PERMISSIONS_SUCCESS);
        filter.addAction(Constants.PERMISSIONS_FAIL);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_send;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (minGasPrice != 5) {
            double d = minGasPrice + 0.2 * progress;
            tv_mining_fee_value.setText(CommonUtils.showDouble(getGasFee(d)) + " eth");
            transactionRecodeEntity.setGasFee(CommonUtils.showDouble(getGasFee(d)));
            gasPrice = d;
        } else {
            double d = minGasPrice + 0.1 * progress;
            tv_mining_fee_value.setText(CommonUtils.showDouble(getGasFee(d)) + " eth");
            transactionRecodeEntity.setGasFee(CommonUtils.showDouble(getGasFee(d)));
            gasPrice = d;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_address:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    CommonUtils.cameraPermission(this);
                } else {
                    Intent intent = new Intent(this, MipcaActivityCapture.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, 1);
                }
                break;
            case R.id.btn_send:
                String to = edt_send_address.getText().toString().trim();
                String from = "0x" + DefApplication.getCurrentUser().getAddress();
                String value = edt_send_amount.getText().toString().trim();
                try {
                    double valueD = Double.parseDouble(value);
                    if (CheckStringUtils.isWalletAddress(to)) {
                        if (to.equals(from)) {
                            warningView = new WarningView(SendActivity.this, getResources().getString(R.string.receiva_same_to_send));
                            CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                        } else {
                            authenticationDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
                            authenticationDialog.setCanceledOnTouchOutside(false);
                            authenticationDialog.setCancelable(false);
                            authenticationDialog.show();
                            if (authenticationDialog.getEditText() != null) {
                                passwordEditText = authenticationDialog.getEditText();
                            }
                            if (authenticationDialog.getTv_ok() != null) {
                                authenticationDialog.getTv_ok().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CommonUtils.buildProgressDialog(SendActivity.this, progressDialog, R.string.validate_password);
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                if (helper.validatePassword(DefApplication.getCurrentUser().getAddress(), passwordEditText.getText().toString().trim())) {
                                                    CommonUtils.cancelProgressDialog(progressDialog);
                                                    password = passwordEditText.getText().toString().trim();
                                                    authenticationDialog.dismiss();
                                                    mHandler.sendEmptyMessage(PASSWORDSUCCESS);
                                                } else {
                                                    CommonUtils.cancelProgressDialog(progressDialog);
                                                    mHandler.sendEmptyMessage(PASSWORDFAILED);
                                                }
                                            }
                                        }.start();
                                    }
                                });
                            }
                            if (authenticationDialog.getTv_cancel() != null) {
                                authenticationDialog.getTv_cancel().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        passwordEditText.setText("");
                                        authenticationDialog.dismiss();
                                    }
                                });
                            }
                        }
                    } else {
                        warningView = new WarningView(SendActivity.this, getResources().getString(R.string.wallet_address_format_wrong));
                        CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                    }
                } catch (Exception e) {
                    warningView = new WarningView(SendActivity.this, getResources().getString(R.string.send_value_format_wrong));
                    CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
                }

                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        btn_send.setEnabled(false);
        btn_send.setBackgroundResource(R.drawable.shape_button3);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!edt_send_address.getText().toString().equals("") && !edt_send_amount.getText().toString().equals("") && hasNonce) {
            btn_send.setEnabled(true);
            btn_send.setBackgroundResource(R.drawable.shape_button);
        }
    }

    class MBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.PERMISSIONS_SUCCESS)) {
                Intent mIntent = new Intent(SendActivity.this, MipcaActivityCapture.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(mIntent, 1);
            } else if (intent.getAction().equals(Constants.PERMISSIONS_FAIL)) {
                warningView = new WarningView(SendActivity.this, getResources().getString(R.string.no_permission));
                CommonUtils.buildWarning(SendActivity.this, rl_send, warningView, mHandler);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    edt_send_address.setText(bundle.getString("result"));
                }
                break;
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

    private double getMinGasPrice(double gasPrice) {
        double result = gasPrice - 10;
        if (result < 5) {
            result = 5d;
        }
        return result;
    }

    private double getMaxGasPrice(double gasPrice) {
        double result = gasPrice + 10;
        return result;
    }

    private double getGasFee(double gasPrice) {
        double gasFee = (gasLimit * gasPrice * Math.pow(10, 9)) / Math.pow(10, 18);
        return gasFee;
    }
}
