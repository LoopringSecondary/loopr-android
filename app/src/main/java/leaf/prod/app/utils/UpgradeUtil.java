package leaf.prod.app.utils;

import java.io.IOException;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import leaf.prod.app.R;
import leaf.prod.app.receiver.ApkInstallReceiver;
import leaf.prod.walletsdk.model.response.AppResponseWrapper;
import leaf.prod.walletsdk.model.response.app.VersionResp;
import leaf.prod.walletsdk.service.VersionService;
import leaf.prod.walletsdk.util.SPUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-16 3:10 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class UpgradeUtil {

    private static boolean updateHint = false;

    private static VersionService versionService = new VersionService();

    private static DownloadManager downloadManager;

    /**
     * 升级提示框
     */
    public static void showUpdateHint(Context context, boolean force) {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (!updateHint || force) {
            versionService.getNewVersion(null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LyqbLogger.log(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    VersionResp versionResult = null;
                    try {
                        AppResponseWrapper<VersionResp> responseWrapper2 = new Gson().fromJson(result, new TypeToken<AppResponseWrapper<VersionResp>>() {
                        }.getType());
                        versionResult = responseWrapper2 != null && responseWrapper2.getSuccess() ? responseWrapper2.getMessage() : null;
                        if (!force) {
                            String ignoreVersion = (String) SPUtils.get(context, "ignoreVersion", "");
                            if (!ignoreVersion.isEmpty() && ignoreVersion.equals(versionResult.getVersion())) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (versionResult != null && !AndroidUtils.getVersionName(context)
                            .equals(versionResult.getVersion())) {
                        AlertDialog.Builder updateDialog = new AlertDialog.Builder(context);
                        VersionResp finalVersionResult = versionResult;
                        updateDialog.setPositiveButton(context.getResources()
                                .getString(R.string.upgrade_confirm), (dialogInterface, i0) -> {
                            updateHint = true;
                            downloadApk(context, finalVersionResult.getBaiduUri());
                            dialogInterface.dismiss();
                        });
                        updateDialog.setNegativeButton(context.getResources()
                                .getString(R.string.upgrade_cancel), (dialogInterface, i) -> {
                            updateHint = true;
                            SPUtils.put(context, "ignoreVersion", finalVersionResult.getVersion());
                            dialogInterface.dismiss();
                        });
                        updateDialog.setMessage(context.getResources()
                                .getString(R.string.upgrade_tips, versionResult.getVersion()));
                        updateDialog.setTitle(context.getResources().getString(R.string.upgrade_title));
                        Looper.prepare();
                        updateDialog.show();
                        Looper.loop();
                    }
                }
            });
        }
        clearApk();
    }

    public static String getNewVersion(Context context) {
        return (String) SPUtils.get(context, "ignoreVersion", "");
    }

    /**
     * 下载apk
     *
     * @param context
     * @param url
     */
    public static void downloadApk(Context context, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Upwallet.apk")
                .setMimeType("application/vnd.android.package-archive");
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ApkInstallReceiver apkInstallReceiver = new ApkInstallReceiver(downloadManager, downloadManager.enqueue(request));
        context.registerReceiver(apkInstallReceiver, filter);
    }

    /**
     * 安装apk
     *
     * @param context
     * @param downloadApkId
     */
    public static void installApk(Context context, long downloadApkId) {
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //根据id判断如果文件已经下载成功返回保存文件的路径
        Uri downloadFileUri = dManager.getUriForDownloadedFile(downloadApkId);
        //        SPUtils.put(context, "downloadApkId", downloadApkId);
        install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
        if (downloadFileUri != null) {
            if ((Build.VERSION.SDK_INT >= 24)) {//判读版本是否在7.0以上
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            context.startActivity(install);
        }
    }

    /**
     * 清理之前下载完成的安装包
     */
    private static void clearApk() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_SUCCESSFUL);
        Cursor c = downloadManager.query(query);
        while (c.moveToNext()) {
            downloadManager.remove(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
        }
    }
}