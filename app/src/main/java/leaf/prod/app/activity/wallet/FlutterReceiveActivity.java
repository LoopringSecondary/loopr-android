package leaf.prod.app.activity.wallet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.vondear.rxfeature.tool.RxQRCode;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.flutter.facade.Flutter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.util.WalletUtil;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class FlutterReceiveActivity extends BaseActivity {

    @BindView(R.id.ll_share_view)
    ConstraintLayout llShareView;

    @BindView(R.id.title)
    TitleView title;

    private UMShareListener umShareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            RxToast.success(getResources().getString(R.string.share_success));
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (t.getMessage().contains("2008")) {//错误码
                RxToast.error(getResources().getString(R.string.share_failed_no_app));
            } else {
                RxToast.error(getResources().getString(R.string.share_failed, t.getMessage()));
            }
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            //            Toast.makeText(getContext(), "取消了", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_receive);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        Config.DEBUG = true;

        // TODO: You can see a delay when loading FlutterView
        // This will be fixed when we have a better understanding of Flutter
        String walletAddress = (WalletUtil.getCurrentAddress(this));

        FlutterView flutterView = Flutter.createView(
                this,
                getLifecycle(),
                "qrCode"
        );

        new MethodChannel(flutterView, "qrCodeDisplay").setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        if (call.method.equals("qrCodeDisplay.get")) {
                            String greetings = walletAddress;
                            result.success(greetings);
                        } else if (call.method.equals("qrCodeDisplay.copyAdress")) {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            // 将文本内容放到系统剪贴板里。
                            cm.setText(walletAddress);
                            RxToast.success(getResources().getString(R.string.copy_to_clipborad_success));
                        } else if (call.method.equals("qrCodeDisplay.saveToAlbum")) {
                            Bitmap bitmap = getBitmap(llShareView);
                            boolean saveChartResult = saveChart(bitmap, llShareView.getMeasuredHeight(), llShareView.getMeasuredWidth());
                            if (saveChartResult) {
                                RxToast.success(getResources().getString(R.string.save_pic_success));
                            }
                        }
                    }
                }
        );

        // Define layout for Flutter
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels-260);
        layout.topMargin = 260;
        this.addContentView(flutterView, layout);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.receive_code));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_share, button -> {
            FlutterReceiveActivityPermissionsDispatcher.showWriteWithPermissionCheck(FlutterReceiveActivity.this);
        });


    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {


    }

    // 单个权限
    // @NeedsPermission(Manifest.permission.CAMERA)
    // 多个权限
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showWrite() {
        uShare();
    }

    // 用户拒绝授权回调（可选）
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForWrite() {
        RxToast.error(getResources().getString(R.string.no_access));
    }

    // 用户勾选了“不再提醒”时调用（可选）
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForWrite() {
        RxToast.error(getResources().getString(R.string.no_access_no_alert));
    }

    private void uShare() {
        UMImage umImage = new UMImage(getApplicationContext(), getBitmap(llShareView));
        umImage.setTitle(getResources().getString(R.string.wallet_address_share));//标题
        umImage.setDescription(getResources().getString(R.string.wallet_address_share));
        ShareAction shareAction = new ShareAction(FlutterReceiveActivity.this);
        shareAction.setDisplayList(SHARE_MEDIA.QQ,
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.FACEBOOK)//传入平台
                .setCallback(umShareListener).withMedia(umImage).open();
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FlutterReceiveActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    // 根据 layout 生成bitmap
    public Bitmap getBitmap(ConstraintLayout layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);
        return bmp;
    }

    // 将bitmap保存至sd card
    public boolean saveChart(Bitmap getbitmap, float height, float width) {
        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File file = new File(folder.getPath() + File.separator + "/" + timeStamp + ".png");
        if (!file.exists()) {
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream ostream;
        try {
            ostream = new FileOutputStream(file);
            Bitmap well = getbitmap;
            Bitmap save = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            Canvas now = new Canvas(save);
            now.drawRect(new Rect(0, 0, (int) width, (int) height), paint);
            now.drawBitmap(well,
                    new Rect(0, 0, well.getWidth(), well.getHeight()),
                    new Rect(0, 0, (int) width, (int) height), null);
            if (save == null) {
                System.out.println("NULL bitmap save\n");
            }
            save.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }
}
