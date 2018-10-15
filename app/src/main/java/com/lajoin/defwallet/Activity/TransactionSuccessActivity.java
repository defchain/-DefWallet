package com.lajoin.defwallet.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lajoin.defwallet.DefApplication;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.entity.TransactionResultEntity;
import com.lajoin.defwallet.utils.CommonUtils;
import com.lajoin.defwallet.utils.LogUtils;
import com.lajoin.defwallet.utils.QRCodeUtil;

import java.io.File;

public class TransactionSuccessActivity extends BaseActivity {
    private TextView tv_value, tv_from_address, tv_to_address, tv_gas_fee, tv_memo, tv_transaction_time, tv_order;
    private RelativeLayout rl_copy_address;
    private ImageView img_qrcode;
    private TransactionResultEntity transactionResultEntity;
    private String filePath;
    private Toolbar mToolbar;
    private RelativeLayout rl_transaction_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_success);
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case 1:
                img_qrcode.setImageBitmap(BitmapFactory.decodeFile(filePath));
                break;
        }
    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_transaction_success);
        rl_transaction_record=findViewById(R.id.rl_transaction_record);
        CommonUtils.buildDefaultToolbar(this, mToolbar);

        if (DefApplication.getTransactionResultEntity() != null) {
            transactionResultEntity = DefApplication.getTransactionResultEntity();
        } else {
            LogUtils.log("transactionResultEntity=null");
        }

        tv_value = findViewById(R.id.tv_value);
        tv_from_address = findViewById(R.id.tv_from_address);
        tv_to_address = findViewById(R.id.tv_to_address);
        tv_gas_fee = findViewById(R.id.tv_gas_fee);
        tv_memo = findViewById(R.id.tv_memo);
        tv_transaction_time = findViewById(R.id.tv_transaction_time);
        tv_order = findViewById(R.id.tv_order);
        rl_copy_address = findViewById(R.id.rl_copy_address);
        img_qrcode = findViewById(R.id.img_qrcode);

        tv_value.setText(transactionResultEntity.getValue());
        tv_from_address.setText("0x" + DefApplication.getCurrentUser().getAddress());
        tv_to_address.setText(transactionResultEntity.getToAddress());
        tv_gas_fee.setText(transactionResultEntity.getGasFee());
        tv_memo.setText(transactionResultEntity.getMemo());
        tv_transaction_time.setText(transactionResultEntity.getTime());
        tv_order.setText(transactionResultEntity.getOrder());
        filePath = CommonUtils.getFileRoot(this)
                + File.separator + "qr_" + System.currentTimeMillis()
                + ".jpg";
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean success = QRCodeUtil.createQRImage(
                        transactionResultEntity.getUrl().trim(),
                        800,
                        800,
                        null,
                        filePath);

                if (success) {
                    mHandler.sendEmptyMessage(1);
                }
            }
        }).start();

        rl_copy_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", transactionResultEntity.getUrl().trim());
                cm.setPrimaryClip(mClipData);
                showToast(R.string.copy_success);
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
        return rl_transaction_record;
    }

}
