package leaf.prod.app.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.umeng.analytics.MobclickAgent;

import leaf.prod.app.R;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.receiver.NetworkStateReceiver;
import leaf.prod.app.receiver.NotificationReceiver;
import leaf.prod.app.utils.NetworkUtil;
import leaf.prod.app.utils.SystemStatusManager;
import leaf.prod.app.view.APP;
import leaf.prod.app.view.Operation;
import leaf.prod.app.views.swipeback.SwipeBackActivity;
import leaf.prod.app.views.swipeback.SwipeBackLayout;
import leaf.prod.walletsdk.manager.LoginDataManager;
import leaf.prod.walletsdk.model.Network;

public abstract class BaseActivity extends SwipeBackActivity {

    private static Map<String, Dialog> networkDialogMap;

    public SwipeBackLayout mSwipeBackLayout;

    public BasePresenter presenter;

    protected LoginDataManager loginDataManager;

    protected Point mPoint = new Point();

    /**
     * 当前Activity的弱引用，防止内存泄露
     **/
    private WeakReference<Activity> context = null;

    /**
     * 共通操作
     **/
    private Operation mBaseOperation = null;

    //	private AlertDialog progressDialog;
    private ProgressDialog progressDialog;

    private MainNetworkReceiver mainNetworkReceiver;

    private NotificationReceiver notificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        //将当前Activity压入栈
        context = new WeakReference<Activity>(this);
        mBaseOperation = new Operation(this);
        initPresenter();
        initTitle();
        initView();
        initData();
        initListener();
        showNetworkDialog(this.getLocalClassName(), false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public WeakReference<Activity> getWContext() {
        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        Dialog dialog = networkDialogMap.get(this.getLocalClassName());
        if (NetworkUtil.getNetWorkState(this) != Network.NETWORK_NONE && dialog != null && dialog.isShowing()) {
            dialog.hide();
        } else if (NetworkUtil.getNetWorkState(this) == Network.NETWORK_NONE && dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.translate_between_interface_left_in, R.anim.translate_between_interface_right_out);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    /**
     * 初始化P层
     */
    protected abstract void initPresenter();

    /**
     * 初始化标题
     */
    public abstract void initTitle();

    /**
     * 初始化视图
     */
    public abstract void initView();

    /**
     * 0
     * 初始化数据
     */
    public abstract void initData();

    private void initListener() {
        mainNetworkReceiver = MainNetworkReceiver.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mainNetworkReceiver, intentFilter);
        notificationReceiver = NotificationReceiver.getInstance(this);
        intentFilter = new IntentFilter("leaf.prod.app.receiver.NotificationReceiver");
        this.registerReceiver(notificationReceiver, intentFilter);
    }

    public void showNetworkDialog(String key, boolean show) {
        if (networkDialogMap == null) {
            networkDialogMap = new HashMap<>();
        }
        if (networkDialogMap.get(key) == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.TopHintDialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_network_status, null);
            builder.setView(view);
            Dialog networkDialog = builder.create();
            networkDialog.setCancelable(false);
            Window window = networkDialog.getWindow();
            assert window != null;
            window.setGravity(Gravity.TOP);
            window.setWindowAnimations(R.style.Animation_TopHint);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            networkDialogMap.put(key, networkDialog);
        }
        if (show) {
            networkDialogMap.get(key).show();
        } else {
            networkDialogMap.get(key).hide();
            this.onResume();
        }
    }

    /**
     * 设置状态栏背景状态
     */
    @SuppressLint("InlinedApi")
    private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        winParams.flags |= bits;
        win.setAttributes(winParams);
        SystemStatusManager tintManager = new SystemStatusManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0);// 状态栏无背景
    }

    @Override
    public APP getApplicationContext() {
        return (APP) super.getApplicationContext();
    }

    /**
     * 获取共通操作机能
     */
    public Operation getOperation() {
        return this.mBaseOperation;
    }

    /**
     * 当ui元素都不可见时，如切换到另一个app，进行资源释放操作
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_UI_HIDDEN:
                // 进行资源释放操作
                break;
        }
    }

    public void showProgress(int messageResId) {
        showProgress(getString(messageResId));
    }

    public void showProgress(String message) {
        showProgress(message, false);
    }

    public void showProgress(String message, boolean cancelable) {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(message);
                progressDialog.setCancelable(cancelable);
                progressDialog.show();
            } else {
                progressDialog.setCancelable(cancelable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideProgress() {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
                progressDialog = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkDialogMap != null && networkDialogMap.get(this.getLocalClassName()) != null) {
            networkDialogMap.get(this.getLocalClassName()).cancel();
            networkDialogMap.put(this.getLocalClassName(), null);
        }
        if (mainNetworkReceiver != null) {
            this.unregisterReceiver(mainNetworkReceiver);
        }
        if (notificationReceiver != null) {
            this.unregisterReceiver(notificationReceiver);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mPoint.x = (int) ev.getRawX();
            mPoint.y = (int) ev.getRawY();
        }
        return super.dispatchTouchEvent(ev);
    }

    private static class MainNetworkReceiver extends NetworkStateReceiver {

        private static boolean network_status = true;

        private static MainNetworkReceiver mainNetworkReceiver;

        private Context context;

        private MainNetworkReceiver(Context context) {
            this.context = context;
        }

        public static MainNetworkReceiver getInstance(Context context) {
            if (mainNetworkReceiver == null) {
                return new MainNetworkReceiver(context);
            }
            return mainNetworkReceiver;
        }

        @Override
        public void doNetWorkNone() {
            network_status = false;
            Activity activity = APP.getCurrentActivity();
            if (activity != null) {
                ((BaseActivity) context).showNetworkDialog(activity.getLocalClassName(), true);
            }
        }

        @Override
        public void doNetWorkWifi() {
            if (network_status)
                return;
            Activity activity = APP.getCurrentActivity();
            if (activity != null) {
                ((BaseActivity) context).showNetworkDialog(activity.getLocalClassName(), false);
            }
            network_status = true;
        }

        @Override
        public void doNetWorkMobile() {
            doNetWorkWifi();
        }
    }
}
