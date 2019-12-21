package leaf.prod.app.activity.wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dtr.zbar.build.ZBarDecoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.vondear.rxfeature.module.scaner.BitmapLuminanceSource;
import com.vondear.rxfeature.module.scaner.CameraManager;
import com.vondear.rxfeature.module.scaner.OnRxScanerListener;
import com.vondear.rxfeature.module.scaner.decoding.InactivityTimer;
import com.vondear.rxtool.RxAnimationTool;
import com.vondear.rxtool.RxBarTool;
import com.vondear.rxtool.RxBeepTool;
import com.vondear.rxtool.RxPhotoTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.activity.ActivityBase;
import com.vondear.rxui.view.dialog.RxDialogSure;
import com.zbar.lib.ZbarManager;

import leaf.prod.app.R;
import leaf.prod.app.utils.QRCodeUitl;

/**
 * @author vondear
 */
public class ActivityScanerCode extends ActivityBase {

    /**
     * 扫描结果监听
     */
    private static OnRxScanerListener mScanerListener;

    private InactivityTimer inactivityTimer;

    /**
     * 扫描处理
     */
    private CaptureActivityHandler handler;

    /**
     * 整体根布局
     */
    private RelativeLayout mContainer = null;

    /**
     * 扫描框根布局
     */
    private RelativeLayout mCropLayout = null;

    private int x = 0;

    private int y = 0;

    /**
     * 扫描边界的宽度
     */
    private int cropWidth = 0;

    /**
     * 扫描边界的高度
     */
    private int cropHeight = 0;

    /**
     * 是否有预览
     */
    private boolean hasSurface;

    /**
     * 扫描成功后是否震动
     */
    private boolean vibrate = true;

    /**
     * 闪光灯开启状态
     */
    private boolean mFlashing = true;

    /**
     * 扫描结果显示框
     */
    private RxDialogSure rxDialogSure;

    private MultiFormatReader multiFormatReader;

    /**
     * 合法的二维码类型
     */
    private String restrictQRCodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBarTool.setNoTitle(this);
        setContentView(com.vondear.rxfeature.R.layout.activity_scaner_code);
        RxBarTool.setTransparentStatusBar(this);
        //界面控件初始化
        initDecode();
        initView();
        //权限初始化
        initPermission();
        //扫描动画初始化
        initScanerAnimation();
        //初始化 CameraManager
        CameraManager.init(mContext);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        restrictQRCodes = getIntent().getStringExtra("restrict");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void initDecode() {
        multiFormatReader = new MultiFormatReader();
        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            Vector<BarcodeFormat> PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
            // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
            Vector<BarcodeFormat> ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
            ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
            ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
            ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
            ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
            ONE_D_FORMATS.add(BarcodeFormat.ITF);
            Vector<BarcodeFormat> QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
            QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
            Vector<BarcodeFormat> DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
            DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(ONE_D_FORMATS);
            decodeFormats.addAll(QR_CODE_FORMATS);
            decodeFormats.addAll(DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = findViewById(com.vondear.rxfeature.R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //Camera初始化
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (!hasSurface) {
                        hasSurface = true;
                        initCamera(holder);
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    hasSurface = false;
                }
            });
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        mScanerListener = null;
        super.onDestroy();
    }

    private void initView() {
        mContainer = findViewById(com.vondear.rxfeature.R.id.capture_containter);
        mCropLayout = findViewById(com.vondear.rxfeature.R.id.capture_crop_layout);
    }

    private void initPermission() {
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat
                .checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initScanerAnimation() {
        ImageView mQrLineView = findViewById(com.vondear.rxfeature.R.id.capture_scan_line);
        RxAnimationTool.ScaleUpDowm(mQrLineView);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    public void btn(View view) {
        int viewId = view.getId();
        if (viewId == com.vondear.rxfeature.R.id.top_mask) {
            light();
        } else if (viewId == com.vondear.rxfeature.R.id.top_back) {
            finish();
        } else if (viewId == com.vondear.rxfeature.R.id.top_openpicture) {
            RxPhotoTool.openLocalImage(mContext);
        }
    }

    private void light() {
        if (mFlashing) {
            mFlashing = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            mFlashing = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            Point point = CameraManager.get().getCameraResolution();
            int width = point.y;
            int height = point.x;
            int x = mCropLayout.getLeft() * width / mContainer.getWidth();
            int y = mCropLayout.getTop() * height / mContainer.getHeight();
            int cropWidth = mCropLayout.getWidth() * width
                    / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height
                    / mContainer.getHeight();
            setX(x);
            setY(y);
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler();
        }
    }

    //--------------------------------------打开本地图片识别二维码 start---------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ContentResolver resolver = getContentResolver();
            // 照片的原始资源地址
            try {
                // 使用ContentProvider通过URI获取原始图片
                Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, data.getData());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                byte[] bitmapData = bytes.toByteArray();
                photo = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                // 开始对图像资源解码
                try {
                    Result rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(photo))));
                    if (rawResult != null && QRCodeUitl.isValidQRCode(rawResult.toString(), restrictQRCodes)) {
                        if (mScanerListener == null) {
                            //                        initDialogResult(rawResult);
                            RxBeepTool.playBeep(mContext, vibrate);
                            initActivityResult(rawResult.getText());
                        } else {
                            mScanerListener.onSuccess("From to Picture", rawResult);
                        }
                    } else {
                        if (mScanerListener == null) {
                            RxToast.error("图片识别失败");
                        } else {
                            mScanerListener.onFail("From to Picture", "图片识别失败");
                        }
                    }
                } catch (Exception e) {
                    RxToast.error("图片识别失败");
                    Log.e("", e.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //========================================打开本地图片识别二维码 end=================================

    /**
     * 将结果返回到跳转的activity
     */
    private void initActivityResult(String result) {
        Intent intent = new Intent();
        intent.putExtra("result", result);
        setResult(RESULT_OK, intent);
        finish();
    }
    //==============================================================================================解析结果 及 后续处理 end

    public void handleDecode(String result) {
        inactivityTimer.onActivity();
        //扫描成功之后的振动与声音提示
        RxBeepTool.playBeep(mContext, vibrate);
        String content = result.replaceFirst(".*?:", "");
        Log.v("二维码/条形码 扫描结果", content);
        if (mScanerListener == null) {
            RxToast.success("扫描成功");
            initActivityResult(content);
        }
    }

    private void decode(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;
        ZbarManager manager = new ZbarManager();
        String result = manager.decode(rotatedData, width, height, true,
                x, y, cropWidth, cropHeight);
        ZBarDecoder zBarDecoder = new ZBarDecoder();
        String result_line = zBarDecoder.decodeRaw(rotatedData, width, height);
        if (handler == null)
            return;
        if (result != null) {
            if (!QRCodeUitl.isValidQRCode(result, restrictQRCodes)) {
                handler.sendEmptyMessage(R.id.decode_failed);
            } else {
                Message msg = new Message();
                msg.obj = "二维码:" + result;
                msg.what = R.id.decode_succeeded;
                handler.sendMessage(msg);
            }
        } else if (result_line != null) {
            if (!QRCodeUitl.isValidQRCode(result_line, restrictQRCodes)) {
                handler.sendEmptyMessage(R.id.decode_failed);
            } else {
                Message msg = new Message();
                msg.obj = "条形码:" + result_line;
                msg.what = R.id.decode_succeeded;
                handler.sendMessage(msg);
            }
        } else {
            handler.sendEmptyMessage(R.id.decode_failed);
        }
    }

    private enum State {
        //预览
        PREVIEW, //成功
        SUCCESS, //完成
        DONE
    }

    final class CaptureActivityHandler extends Handler {

        DecodeThread decodeThread = null;

        private State state;

        public CaptureActivityHandler() {
            decodeThread = new DecodeThread();
            decodeThread.start();
            state = State.SUCCESS;
            CameraManager.get().startPreview();
            restartPreviewAndDecode();
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == com.vondear.rxfeature.R.id.auto_focus) {
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, com.vondear.rxfeature.R.id.auto_focus);
                }
            } else if (message.what == com.vondear.rxfeature.R.id.restart_preview) {
                restartPreviewAndDecode();
            } else if (message.what == com.vondear.rxfeature.R.id.decode_succeeded) {
                state = State.SUCCESS;
                handleDecode((String) message.obj);// 解析成功，回调
            } else if (message.what == com.vondear.rxfeature.R.id.decode_failed) {
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), com.vondear.rxfeature.R.id.decode);
            }
        }

        public void quitSynchronously() {
            state = State.DONE;
            decodeThread.interrupt();
            CameraManager.get().stopPreview();
            removeMessages(com.vondear.rxfeature.R.id.decode_succeeded);
            removeMessages(com.vondear.rxfeature.R.id.decode_failed);
            removeMessages(com.vondear.rxfeature.R.id.decode);
            removeMessages(com.vondear.rxfeature.R.id.auto_focus);
        }

        private void restartPreviewAndDecode() {
            if (state == State.SUCCESS) {
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), com.vondear.rxfeature.R.id.decode);
                CameraManager.get().requestAutoFocus(this, com.vondear.rxfeature.R.id.auto_focus);
            }
        }
    }

    final class DecodeThread extends Thread {

        private final CountDownLatch handlerInitLatch;

        private Handler handler;

        DecodeThread() {
            handlerInitLatch = new CountDownLatch(1);
        }

        Handler getHandler() {
            try {
                handlerInitLatch.await();
            } catch (InterruptedException ie) {
                // continue?
            }
            return handler;
        }

        @Override
        public void run() {
            Looper.prepare();
            handler = new DecodeHandler();
            handlerInitLatch.countDown();
            Looper.loop();
        }
    }

    final class DecodeHandler extends Handler {

        DecodeHandler() {
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == com.vondear.rxfeature.R.id.decode) {
                decode((byte[]) message.obj, message.arg1, message.arg2);
            } else if (message.what == com.vondear.rxfeature.R.id.quit) {
                Looper.myLooper().quit();
            }
        }
    }
}
