package leaf.prod.app.activity.setupWallet;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewPager;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.wallet.ActivityScanerCode;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.fragment.setupwallet.ImportKeystoreFragment;
import leaf.prod.app.fragment.setupwallet.ImportMnemonicFragment;
import leaf.prod.app.fragment.setupwallet.ImportPrivateKeyFragment;
import leaf.prod.app.utils.AppManager;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.QRCodeType;
import leaf.prod.walletsdk.model.eventbusData.KeystoreData;
import leaf.prod.walletsdk.util.StringUtils;

public class ImportWalletActivity extends BaseActivity {

    private static int REQUEST_CODE = 1;  //二维码扫一扫code

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private List<Fragment> mFragments;

    private String[] mTitles = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_import_wallet);
        ButterKnife.bind(this);
        AppManager.getAppManager().addActivity(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
        title.setBTitle(getResources().getString(R.string.import_wallet));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_scan, button -> {
            Intent intent = new Intent(ImportWalletActivity.this, ActivityScanerCode.class);
            intent.putExtra("restrict", QRCodeType.KEY_STORE.name());
            startActivityForResult(intent, REQUEST_CODE);
        });
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        mTitles[0] = getResources().getString(R.string.mnemonic);
        mTitles[1] = getResources().getString(R.string.keystore);
        mTitles[2] = getResources().getString(R.string.private_key);
        mFragments = new ArrayList<>();
        mFragments.add(new ImportMnemonicFragment());
        mFragments.add(new ImportKeystoreFragment());
        mFragments.add(new ImportPrivateKeyFragment());
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), mFragments, mTitles));
        tabLayout.setupWithViewPager(viewPager);
        if (!StringUtils.isEmpty(getIntent().getStringExtra("result"))) {
            viewPager.setCurrentItem(1);
            viewPager.post(() -> EventBus.getDefault().post(new KeystoreData(getIntent().getStringExtra("result"))));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString("result");
                viewPager.setCurrentItem(1);
                EventBus.getDefault().post(new KeystoreData(result));
                //                if (viewPager.getCurrentItem() == 0) {
                //                    EventBus.getDefault().post(new MnemonicData(result));
                //                } else if (viewPager.getCurrentItem() == 1) {
                //                    EventBus.getDefault().post(new KeystoreData(result));
                //                } else if (viewPager.getCurrentItem() == 2) {
                //                    EventBus.getDefault().post(new PrivateKeyData(result));
                //                }
            }
        }
    }
}
