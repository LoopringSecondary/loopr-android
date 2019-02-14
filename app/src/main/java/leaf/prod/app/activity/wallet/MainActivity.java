package leaf.prod.app.activity.wallet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.fragment.infomation.NewsFragment;
import leaf.prod.app.fragment.setting.SettingFragment;
import leaf.prod.app.fragment.trade.DappFragment;
import leaf.prod.app.fragment.trade.TradeFragment;
import leaf.prod.app.fragment.wallet.MainFragment;
import leaf.prod.app.utils.AppManager;
import leaf.prod.app.utils.UpgradeUtil;
import leaf.prod.walletsdk.model.Language;
import leaf.prod.walletsdk.util.LanguageUtil;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;

/**
 * Created by niedengqiang on 2018/8/13.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_frame)
    FrameLayout mainFrame;

    @BindView(R.id.iv_main)
    ImageView ivMain;

    @BindView(R.id.tv_main)
    TextView tvMain;

    @BindView(R.id.rl_main)
    RelativeLayout rlMain;

    @BindView(R.id.iv_trade)
    ImageView ivTrade;

    @BindView(R.id.tv_trade)
    TextView tvTrade;

    @BindView(R.id.tv_news)
    TextView tvNews;

    @BindView(R.id.rl_dapp)
    RelativeLayout rlDapp;

    @BindView(R.id.rl_trade)
    RelativeLayout rlTrade;

    @BindView(R.id.iv_setting)
    ImageView ivSetting;

    @BindView(R.id.tv_setting)
    TextView tvSetting;

    @BindView(R.id.rl_setting)
    RelativeLayout rlSetting;

    @BindView(R.id.rl_news)
    RelativeLayout rlNews;

    @BindView(R.id.ccl_main)
    LinearLayout cclMain;

    @BindView(R.id.cl_loading)
    ConstraintLayout clLoading;

    @BindView(R.id.ae_loading)
    LottieAnimationView aeLoading;

    private long exitTime = 0;

    private int index;

    private int currentTabIndex;  //当前页

    private ImageView[] imagebuttons; //底部tab-imageview集合

    private TextView[] textviews; //底部tab-textview集合

    private Fragment fragment1, fragment2, fragment3, fragment4, fragment5;

    private MyTouchListener myTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppManager.getAppManager().addActivity(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        UpgradeUtil.showUpdateHint(this, false);
    }

    @Override
    public void initView() {
        changeFragment();
        setTabSelect(0);
        if ((boolean) SPUtils.get(this, "isRecreate", false)) {//判断是否是更改语言设置后，执行了系统的recreate()方法,
            ChangeMainFragment(3);
            SPUtils.put(this, "isRecreate", false);
        }
        if (WalletUtil.getCurrentWallet(this).getAmount() >= 30) {
            rlTrade.setVisibility(View.VISIBLE);
            rlDapp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initData() {
    }

    @Override
    public void initTitle() {
    }

    public void setTabSelect(int i) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch (i) {
            case 0:
                fragment1 = manager.findFragmentByTag("TAG1");
                hideTab(transaction);
                if (fragment1 == null) {
                    fragment1 = new MainFragment();
                    transaction.add(R.id.main_frame, fragment1, "TAG1");
                } else {
                    transaction.show(fragment1);
                }
                break;
            case 1:
                fragment2 = manager.findFragmentByTag("TAG2");
                hideTab(transaction);
                if (fragment2 == null) {
                    fragment2 = new TradeFragment();
                    transaction.add(R.id.main_frame, fragment2, "TAG2");
                } else {
                    transaction.show(fragment2);
                }
                break;
            case 2:
                fragment3 = manager.findFragmentByTag("TAG3");
                hideTab(transaction);
                if (fragment3 == null) {
                    fragment3 = new DappFragment();
                    transaction.add(R.id.main_frame, fragment3, "TAG3");
                } else {
                    transaction.show(fragment3);
                }
                break;
            case 3:
                fragment4 = manager.findFragmentByTag("TAG4");
                hideTab(transaction);
                if (fragment4 == null) {
                    fragment4 = new NewsFragment();
                    transaction.add(R.id.main_frame, fragment4, "TAG4");
                } else {
                    transaction.show(fragment4);
                }
                break;
            case 4:
                fragment5 = manager.findFragmentByTag("TAG4");
                hideTab(transaction);
                if (fragment5 == null) {
                    fragment5 = new SettingFragment();
                    transaction.add(R.id.main_frame, fragment5, "TAG4");
                } else {
                    transaction.show(fragment5);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideTab(FragmentTransaction transaction) {
        if (fragment1 != null) {
            transaction.hide(fragment1);
        }
        if (fragment2 != null) {
            transaction.hide(fragment2);
        }
        if (fragment3 != null) {
            transaction.hide(fragment3);
        }
        if (fragment4 != null) {
            transaction.hide(fragment4);
        }
        if (fragment5 != null) {
            transaction.hide(fragment5);
        }
    }

    private void changeFragment() {
        imagebuttons = new ImageView[5];
        imagebuttons[0] = (ImageView) findViewById(R.id.iv_main);
        imagebuttons[1] = (ImageView) findViewById(R.id.iv_trade);
        imagebuttons[2] = (ImageView) findViewById(R.id.iv_dapp);
        imagebuttons[3] = (ImageView) findViewById(R.id.iv_news);
        imagebuttons[4] = (ImageView) findViewById(R.id.iv_setting);
        imagebuttons[0].setSelected(true);
        textviews = new TextView[5];
        textviews[0] = (TextView) findViewById(R.id.tv_main);
        textviews[1] = (TextView) findViewById(R.id.tv_trade);
        textviews[2] = (TextView) findViewById(R.id.tv_dapp);
        textviews[3] = (TextView) findViewById(R.id.tv_news);
        textviews[4] = (TextView) findViewById(R.id.tv_setting);
        textviews[0].setTextColor(getResources().getColor(R.color.white));
    }

    //改变主界面的fragment
    public void ChangeMainFragment(int type) {
        index = type;
        imagebuttons[currentTabIndex].setSelected(false);
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(getResources().getColor(R.color.colorFortyWhite));
        textviews[index].setTextColor(getResources().getColor(R.color.white));
        currentTabIndex = index;
        setTabSelect(type);
    }

    @OnClick({R.id.rl_main, R.id.rl_dapp, R.id.rl_trade, R.id.rl_news, R.id.rl_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_main:
                index = 0;
                setTabSelect(0);
                break;
            case R.id.rl_trade:
                index = 1;
                setTabSelect(1);
                break;
            case R.id.rl_dapp:
                index = 2;
                setTabSelect(2);
                break;
            case R.id.rl_news:
                index = 3;
                setTabSelect(3);
                break;
            case R.id.rl_setting:
                index = 4;
                setTabSelect(4);
                break;
        }
        imagebuttons[currentTabIndex].setSelected(false);
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(getResources().getColor(R.color.colorFortyWhite));
        textviews[index].setTextColor(getResources().getColor(R.color.white));
        currentTabIndex = index;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((boolean) SPUtils.get(this, "isRecreate", false)) {//判断是否是更改语言设置后，执行了系统的recreate()方法,
            recreate();//判断是否是更改语言设置后，执行了系统的recreate()方法,
        }
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        index = 0;
        setTabSelect(0);
        SPUtils.put(this, "isNewintent", true);
        recreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //        APP.getLoopring().destroy();
    }

    /***
     * 返回键按两下退出
     *
     * @return
     * @throws Exception
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (fragment1.isVisible() && ((MainFragment) fragment1).getCurrentItem() == 0) {
                ((MainFragment) fragment1).setItem(new MainFragment.Event(1));
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    RxToast.warning(this, getResources().getString(R.string.click_twice_close), Toast.LENGTH_SHORT)
                            .show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showLoading(boolean show) {
        clLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showBottomBar(boolean show) {
        cclMain.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 在fragment回调触摸事件获得坐标
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (null != myTouchListener) {
            myTouchListener.onTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registerMyTouchListener(MyTouchListener myTouchListener) {
        this.myTouchListener = myTouchListener;
    }

    public interface MyTouchListener {

        void onTouch(MotionEvent ev);
    }

    @Override
    protected void attachBaseContext(Context base) {
        Language language = LanguageUtil.getSettingLanguage(base);
        if (language != null) {
            super.attachBaseContext(LanguageUtil.changeLanguage(base, language));
        }
    }
}
