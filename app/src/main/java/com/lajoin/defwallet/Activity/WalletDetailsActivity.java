package com.lajoin.defwallet.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.Constants;
import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.ethUtils.ETHWebService;
import com.lajoin.defwallet.ethUtils.ETHWebServiceListener;
import com.lajoin.defwallet.utils.CheckStringUtils;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.CompanyUtil;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.utils.WalletAssetsSharedPreferencesUtils;
import com.lajoin.defwallet.view.AuthenticationDialog;
import com.lajoin.defwallet.view.DeleteWalletDialog;
import com.lajoin.defwallet.view.ExportPrivateKeyDialog;

import org.json.JSONException;

import java.util.List;

import io.defchain.defwallet.DEFWallet;
import io.defchain.defwallet.DEFWalletHelper;

/**
 * Created by hasee on 2018/7/4.
 */

public class WalletDetailsActivity extends BaseActivity implements View.OnClickListener {
    private static final int PASSWORDSUCCESS = 1;
    private static final int PASSWORDFAILED = 2;
    private static final int EXPORTPRIVATEKEYSUCCESS = 3;
    private static final int EXPORTPRIVATEKEYFAILED = 4;
    private static final int DELETEWALLETSUCCESS = 5;
    private static final int DELETEWALLETFAILED = 6;
    private static final int MODIFYWALLETNAMESUCCESS = 7;
    private static final int MODIFYWALLETNAMEFAILED = 8;

    private String walletName, walletAddress, walletValue;
    private TextView tv_wallet_name, tv_wallet_address, tv_assets_value;
    private RelativeLayout rl_edit_password, rl_export_private_key, rl_export_keystore, rl_delete_wallet;
    private AuthenticationDialog authenticationDialog;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;
    private DEFWalletHelper helper;
    private String password;
    private ExportPrivateKeyDialog exportPrivateKeyDialog;
    private DeleteWalletDialog deleteWalletDialog;
    private Toolbar mToolbar;
    private ImageView img_wallet_name;
    private EditText edt_wallet_name;
    private RelativeLayout edtRelativeLayout;
    private List<DEFWallet> mDEFWalletList;
    private boolean isSameName;
    private AlertDialog.Builder alertDialogBuilder;
    private Dialog mDialog;
    private RelativeLayout rl_wallet_details;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walletdetails);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case PASSWORDSUCCESS:
                int type = (int) msg.obj;
                if (type == 1) {
                    CommonUtils.buildProgressDialog(this, progressDialog, R.string.exporting_private_key);
                    new Thread() {
                        @Override
                        public void run() {
                            String privateKey = helper.exportWalletPrivateKey(walletAddress, password);
                            if (privateKey != null && !privateKey.equals("")) {
                                Message msg = Message.obtain();
                                msg.what = EXPORTPRIVATEKEYSUCCESS;
                                msg.obj = privateKey;
                                mHandler.sendMessage(msg);
                            } else {
                                mHandler.sendEmptyMessage(EXPORTPRIVATEKEYFAILED);
                            }
                        }
                    }.start();
                } else if (type == 2) {
                    startActivity(new Intent(this, ExportKeystoreActivity.class).putExtra("walletAddress", walletAddress));
                } else if (type == 3) {
                    deleteWalletDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
                    deleteWalletDialog.setCanceledOnTouchOutside(false);
                    deleteWalletDialog.setCancelable(false);
                    deleteWalletDialog.show();
                    if (deleteWalletDialog.getBtn_ok() != null) {
                        deleteWalletDialog.getBtn_ok().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isSameName = false;
                                CommonUtils.buildProgressDialog(WalletDetailsActivity.this, progressDialog, R.string.deleting_wallet);
                                new Thread() {
                                    @Override
                                    public void run() {
                                        if (helper.deleteWallet(walletAddress)) {
                                            mHandler.sendEmptyMessage(DELETEWALLETSUCCESS);
                                        } else {
                                            mHandler.sendEmptyMessage(DELETEWALLETFAILED);
                                        }
                                    }
                                }.start();
                            }
                        });
                    }
                }

                break;
            case PASSWORDFAILED:
                showToast(R.string.password_wrong);
                break;
            case EXPORTPRIVATEKEYSUCCESS:
                final String privateKey = (String) msg.obj;
                CommonUtils.cancelProgressDialog(progressDialog);
                exportPrivateKeyDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
                exportPrivateKeyDialog.show();
                if (exportPrivateKeyDialog.getTv_privateKey() != null) {
                    exportPrivateKeyDialog.getTv_privateKey().setText(privateKey);
                }
                if (exportPrivateKeyDialog.getBtn_copy() != null) {
                    exportPrivateKeyDialog.getBtn_copy().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("Label", privateKey);
                            cm.setPrimaryClip(mClipData);
                            exportPrivateKeyDialog.dismiss();
                        }
                    });
                }
                break;
            case EXPORTPRIVATEKEYFAILED:
                CommonUtils.cancelProgressDialog(progressDialog);
                showToast(R.string.export_private_failed);
                break;
            case DELETEWALLETSUCCESS:
                WalletAssetsSharedPreferencesUtils.getInstance(this).deleteWalletRecode(walletAddress);
                CommonUtils.cancelProgressDialog(progressDialog);
                finish();
                break;
            case DELETEWALLETFAILED:
                CommonUtils.cancelProgressDialog(progressDialog);
                showToast(R.string.delete_wallet_failed);
                break;
            case Constants.GETLISTSUCCESS:
                final String newWalletName = (String) msg.obj;
                for (int i = 0; i < mDEFWalletList.size(); i++) {
                    if (mDEFWalletList.get(i).getWalletName().equals(newWalletName)) {
                        isSameName = true;
                    }
                }
                if (!isSameName) {
                    new Thread() {
                        @Override
                        public void run() {
                            boolean modifyWalletName = helper.modifyWalletName(walletAddress, newWalletName);
                            if (modifyWalletName) {
                                Message message = Message.obtain();
                                message.what = MODIFYWALLETNAMESUCCESS;
                                message.obj = newWalletName;
                                mHandler.sendMessage(message);
                            } else {
                                mHandler.sendEmptyMessage(MODIFYWALLETNAMEFAILED);
                            }
                        }
                    }.start();
                } else {
                    showToast(R.string.modify_has_same_name);
                }
                break;
            case Constants.GETLISTFAILED:
                showToast(R.string.edit_wallet_name_failed);
                mDialog.dismiss();
                break;
            case MODIFYWALLETNAMESUCCESS:
                String newName = (String) msg.obj;
                showToast(R.string.edit_wallet_name_sucess);
                tv_wallet_name.setText(CommonUtils.showWalletName(newName));
                walletName = newName;
                mDialog.dismiss();
                new Thread() {
                    @Override
                    public void run() {
                        DEFWallet defWallet = helper.findWalletByAddress(walletAddress);
                        if (defWallet != null) {
                            DefApplication.setCurrentUser(defWallet);
                        }
                    }
                }.start();
                break;
            case MODIFYWALLETNAMEFAILED:
                showToast(R.string.edit_wallet_name_failed);
                mDialog.dismiss();
                break;
        }
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        walletName = intent.getStringExtra("walletName");
        walletAddress = intent.getStringExtra("walletAddress").substring(2, intent.getStringExtra("walletAddress").length());
        walletValue = intent.getStringExtra("walletValue");
        mToolbar = findViewById(R.id.toolbar_wallet_details);
        rl_wallet_details = findViewById(R.id.rl_wallet_details);
        CommonUtils.buildDefaultToolbar(this, mToolbar);
        authenticationDialog = new AuthenticationDialog(this);
        progressDialog = new ProgressDialog(this);
        exportPrivateKeyDialog = new ExportPrivateKeyDialog(this);
        deleteWalletDialog = new DeleteWalletDialog(this);
        helper = DEFWalletHelper.getInstance();
        tv_wallet_name = findViewById(R.id.tv_wallet_name);
        tv_wallet_address = findViewById(R.id.tv_wallet_address);
        tv_assets_value = findViewById(R.id.tv_assets_value);
        tv_wallet_name.setText(CommonUtils.showWalletName(walletName));
        if (walletAddress.length() > 25) {
            String front = walletAddress.substring(0, 9);
            String after = walletAddress.substring(walletAddress.length() - 15, walletAddress.length());
            tv_wallet_address.setText("0x" + front + "******" + after);
        } else {
            tv_wallet_address.setText("0x" + walletAddress);
        }
        String value;
        if (walletValue != null) {
            value = walletValue;
        } else {
            value = "0.00";
        }
        String stringRmb = String.format(getResources().getString(R.string.total_assets_item_value), getResources().getString(R.string.rmb_symbol), value);
        String stringUsd = String.format(getResources().getString(R.string.total_assets_item_value), getResources().getString(R.string.usd_symbol), value);
        CommonUtils.setCurrencyShow(tv_assets_value, stringRmb, stringUsd);
        rl_edit_password = findViewById(R.id.rl_edit_password);
        rl_export_private_key = findViewById(R.id.rl_export_private_key);
        rl_export_keystore = findViewById(R.id.rl_export_keystore);
        rl_delete_wallet = findViewById(R.id.rl_delete_wallet);
        img_wallet_name = findViewById(R.id.img_wallet_name);
        rl_edit_password.setOnClickListener(this);
        rl_export_private_key.setOnClickListener(this);
        rl_export_keystore.setOnClickListener(this);
        rl_delete_wallet.setOnClickListener(this);
        img_wallet_name.setOnClickListener(this);

        alertDialogBuilder = new AlertDialog.Builder(this);

    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_wallet_details;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_edit_password:
                startActivity(new Intent(this, EditPasswordActivity.class).putExtra("walletAddress", walletAddress));
                break;
            case R.id.rl_export_private_key:
                validatePassword(1);
                break;
            case R.id.rl_export_keystore:
                validatePassword(2);
                break;
            case R.id.rl_delete_wallet:
                validatePassword(3);
                break;
            case R.id.img_wallet_name:
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                edtRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.view_dialog_edit, null);
                edt_wallet_name = edtRelativeLayout.findViewById(R.id.edt_wallet_name);
                edt_wallet_name.setHint(R.string.edit_new_wallet_name);
                edt_wallet_name.setText(walletName);
                alertDialogBuilder
                        .setTitle(getString(R.string.edit_wallet_name))
                        .setView(edtRelativeLayout)
                        .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String newWalletName = edt_wallet_name.getText().toString().trim();
                                if (!TextUtils.isEmpty(newWalletName) && !newWalletName.equals(walletName)) {
                                    if (CheckStringUtils.isUserName(newWalletName)) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                mDEFWalletList = helper.listAllWallets();
                                                if (mDEFWalletList != null) {
                                                    Message msg = Message.obtain();
                                                    msg.what = Constants.GETLISTSUCCESS;
                                                    msg.obj = newWalletName;
                                                    mHandler.sendMessage(msg);
                                                } else {
                                                    mHandler.sendEmptyMessage(Constants.GETLISTFAILED);
                                                }
                                            }
                                        }.start();
                                    } else {
                                        showToast(R.string.username_format_wrong);
                                        mDialog.dismiss();
                                    }
                                } else {
                                    mDialog.dismiss();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null);
                mDialog = alertDialogBuilder.show();
                break;

        }
    }

    private void validatePassword(final int type) {
        authenticationDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        authenticationDialog.setCanceledOnTouchOutside(false);
        authenticationDialog.setCancelable(false);
        authenticationDialog.show();
        if (authenticationDialog.getEditText() != null) {
            passwordEditText = authenticationDialog.getEditText();
            passwordEditText.setText("");
        }
        if (authenticationDialog.getTv_ok() != null) {

            authenticationDialog.getTv_ok().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.buildProgressDialog(WalletDetailsActivity.this, progressDialog, R.string.validate_password);
                    new Thread() {
                        @Override
                        public void run() {
                            if (helper.validatePassword(walletAddress, passwordEditText.getText().toString().trim())) {
                                CommonUtils.cancelProgressDialog(progressDialog);
                                password = passwordEditText.getText().toString().trim();
                                authenticationDialog.dismiss();
                                Message msg = Message.obtain();
                                msg.what = PASSWORDSUCCESS;
                                msg.obj = type;
                                mHandler.sendMessage(msg);
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
                    authenticationDialog.dismiss();
                }
            });
        }
    }
}
