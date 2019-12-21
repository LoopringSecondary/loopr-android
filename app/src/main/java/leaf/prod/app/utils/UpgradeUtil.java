package leaf.prod.app.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import leaf.prod.app.R;
import leaf.prod.app.receiver.ApkInstallReceiver;
import leaf.prod.walletsdk.model.response.AppResponseWrapper;
import leaf.prod.walletsdk.model.response.app.VersionResp;
import leaf.prod.walletsdk.service.AppService;
import leaf.prod.walletsdk.util.LanguageUtil;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-16 3:10 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class UpgradeUtil {

    private static boolean updateHint = false;

    private static AppService appService = new AppService();

    private static DownloadManager downloadManager;

    private static AlertDialog dialog;

    private static View view;

    private static AlertDialog.Builder builder;

    /**
     * 升级提示框
     */
    public static void showUpdateHint(Context context, boolean force) {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (!updateHint && getIgnoreVersion(context).isEmpty() || force) {
            appService.getLatestVersion(new Callback<AppResponseWrapper<VersionResp>>() {
                @Override
                public void onResponse(Call<AppResponseWrapper<VersionResp>> call, Response<AppResponseWrapper<VersionResp>> response) {
                    try {
                        VersionResp versionResult = response.body().getMessage();
                        if (versionResult != null && AndroidUtils.getVersionName(context)
                                .compareTo(versionResult.getVersion()) < 0) {
                            SPUtils.put(context, "latestVersion", versionResult.getVersion());
                            if (dialog == null) {
                                builder = new AlertDialog.Builder(context, R.style.DialogTheme);
                                view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
                                view.findViewById(R.id.btn_skip).setOnClickListener(v -> {
                                    updateHint = true;
                                    SPUtils.put(context, "ignoreVersion", versionResult.getVersion());
                                    dialog.hide();
                                });
                                view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
                                    updateHint = true;
                                    downloadApk(context, versionResult.getBaiduUri());
                                    dialog.hide();
                                });
                                MarkdownView mvContent = view.findViewById(R.id.mv_content);
                                InternalStyleSheet css = new InternalStyleSheet();
                                css.addRule("body", "background-color:#21203A");
                                css.addRule("body", "margin-bottom: 0px !important");
                                mvContent.addStyleSheet(css);
                                builder.setView(view);
                                dialog = builder.create();
                                dialog.getWindow().setGravity(Gravity.CENTER);
                            }
                            MarkdownView mvContent = view.findViewById(R.id.mv_content);
                            ((TextView) view.findViewById(R.id.tv_version)).setText(versionResult.getVersion());
                            switch (LanguageUtil.getLanguage(context)) {
                                case zh_CN:
                                    mvContent.loadMarkdown(StringUtils.isEmpty(versionResult.getReleaseNoteChs()) ? "" : versionResult
                                            .getReleaseNoteChs());
                                    break;
                                case en_US:
                                    mvContent.loadMarkdown(StringUtils.isEmpty(versionResult.getReleaseNoteEn()) ? "" : versionResult
                                            .getReleaseNoteEn());
                                    break;
                                case zh_Hant:
                                    mvContent.loadMarkdown(StringUtils.isEmpty(versionResult.getReleaseNoteCht()) ? "" : versionResult
                                            .getReleaseNoteCht());
                                    break;
                            }
                            dialog.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<AppResponseWrapper<VersionResp>> call, Throwable t) {
                }
            });
        }
        clearApk();
    }

    public static String getNewVersion(Context context) {
        String version = (String) SPUtils.get(context, "latestVersion", "");
        if (!version.isEmpty() && AndroidUtils.getVersionName(context).compareTo(version) < 0) {
            return version;
        }
        return "";
    }

    public static String getIgnoreVersion(Context context) {
        String version = (String) SPUtils.get(context, "ignoreVersion", "");
        if (!version.isEmpty() && AndroidUtils.getVersionName(context).compareTo(version) < 0) {
            return version;
        }
        return "";
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
