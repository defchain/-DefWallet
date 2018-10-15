package com.lajoin.defwallet.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.lajoin.defwallet.R;
import com.lajoin.defwallet.utils.CommonUtils;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by hasee on 2018/7/6.
 */

public class MipcaActivityCapture extends BaseActivity implements SurfaceHolder.Callback {
    private ViewfinderView viewFinderView;
    private boolean isSurface;
    private InactivityTimer inactivityTimer;
    private CaptureActivityHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private Toolbar mToolbar;
    private RelativeLayout rl_qrcode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        CameraManager.init(this);
        viewFinderView = findViewById(R.id.viewfinder_view);
        isSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surFaceView = findViewById(R.id.preview_view);
        SurfaceHolder holder = surFaceView.getHolder();
        if (isSurface) {
            initCamera(holder);
        } else {
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    protected void initView() {
        mToolbar = findViewById(R.id.toolbar_capture);
        rl_qrcode = findViewById(R.id.rl_qrcode);
        mToolbar.bringToFront();
        CommonUtils.buildDefaultToolbar(this, mToolbar);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void initReceiver() {

    }

    @Override
    protected ViewGroup getRootRelativeLayout() {
        return rl_qrcode;
    }


    public void handleDecode(Result result, Bitmap bit) {
        inactivityTimer.onActivity();
        String resultString = result.getText();
        if ("".equals(resultString)) {
            Toast.makeText(MipcaActivityCapture.this, getResources().getString(R.string.get_qrcode_fail),
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }

        MipcaActivityCapture.this.finish();
    }

    private void initCamera(SurfaceHolder surFaceHolder) {
        try {
            CameraManager.get().openDriver(surFaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }

    }

    public ViewfinderView getViewfinderView() {
        return viewFinderView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isSurface) {
            isSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurface = false;
    }


    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewFinderView.drawViewfinder();
    }
}
