package leaf.prod.app.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import androidx.constraintlayout.ConstraintLayout;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-07 5:31 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class ShareUtil {

    public static void uShareImage(Activity activity, String title, Bitmap bitmap) {
        UMImage umImage = new UMImage(activity, bitmap);
        umImage.setTitle(title);
        umImage.setDescription(title);
        ShareAction shareAction = new ShareAction(activity);
        shareAction.setDisplayList(SHARE_MEDIA.QQ,
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.FACEBOOK)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA platform) {
                    }

                    @Override
                    public void onResult(SHARE_MEDIA platform) {
                        RxToast.success(activity.getResources().getString(R.string.share_success));
                    }

                    @Override
                    public void onError(SHARE_MEDIA platform, Throwable t) {
                        if (t.getMessage().contains("2008")) {//错误码
                            RxToast.error(activity.getResources().getString(R.string.share_failed_no_app));
                        } else {
                            RxToast.error(activity.getResources().getString(R.string.share_failed, t.getMessage()));
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA platform) {
                    }
                }).withMedia(umImage).open();
    }

    public static void uShareUrl(Activity activity, String title, String url, String description, UMShareListener umShareListener) {
        UMWeb umWeb = new UMWeb(url);
        umWeb.setTitle(title);
        umWeb.setDescription(description);
        ShareAction shareAction = new ShareAction(activity);
        shareAction.setDisplayList(SHARE_MEDIA.QQ,
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.FACEBOOK)
                .setCallback(umShareListener).withMedia(umWeb).open();
    }

    public static Bitmap getBitmap(ConstraintLayout layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);
        return bmp;
    }

    // 将bitmap保存至sd card
    public static boolean saveChart(Bitmap bitmap, float height, float width) {
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
            Bitmap well = bitmap;
            Bitmap save = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            Canvas now = new Canvas(save);
            now.drawRect(new Rect(0, 0, (int) width, (int) height), paint);
            now.drawBitmap(well,
                    new Rect(0, 0, well.getWidth(), well.getHeight()),
                    new Rect(0, 0, (int) width, (int) height), null);
            save.compress(Bitmap.CompressFormat.PNG, 100, ostream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }
}
