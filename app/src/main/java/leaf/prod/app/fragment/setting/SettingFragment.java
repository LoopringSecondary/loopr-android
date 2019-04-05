package leaf.prod.app.fragment.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.flutter.facade.Flutter;
import leaf.prod.app.R;
import leaf.prod.app.activity.ShareActivity;
import leaf.prod.app.activity.setting.ContractVersionActivity;
import leaf.prod.app.activity.setting.CurrencyActivity;
import leaf.prod.app.activity.setting.LRCFeeRatioActivity;
import leaf.prod.app.activity.setting.LanguageActivity;
import leaf.prod.app.activity.setting.ManageWalletActivity;
import leaf.prod.app.activity.setting.MarginSplitActivity;
import leaf.prod.app.activity.setting.ThirdLoginActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.utils.AndroidUtils;
import leaf.prod.app.utils.FingerprintUtil;
import leaf.prod.app.utils.UpgradeUtil;
import leaf.prod.walletsdk.manager.LoginDataManager;
import leaf.prod.walletsdk.model.response.AppResponseWrapper;
import leaf.prod.walletsdk.util.SPUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */
public class SettingFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.ll_flutter)
    LinearLayout llFlutter;

    @BindView(R.id.ll_share)
    LinearLayout llShare;

    @BindView(R.id.ll_manager_wallet)
    LinearLayout llManagerWallet;

    @BindView(R.id.ll_money_type)
    LinearLayout llMoneyType;

    @BindView(R.id.ll_language)
    LinearLayout llLanguage;

    @BindView(R.id.ll_id_touch)
    LinearLayout llIdTouch;

    @BindView(R.id.ll_contract_version)
    LinearLayout llContractVersion;

    @BindView(R.id.ll_lrc_proportion)
    LinearLayout llLrcProportion;

    @BindView(R.id.ll_margin_split)
    LinearLayout llMarginSplit;

    @BindView(R.id.app_version)
    TextView appVersion;

    @BindView(R.id.ll_app_version)
    LinearLayout llAppVersion;

    @BindView(R.id.s_v)
    SwitchButton aSwitch;

    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.new_version)
    TextView newVerson;

    private MainActivity mainActivity;

    private AlertDialog.Builder confirmThirdLogin;

    private LoginDataManager loginDataManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void initView() {
        loginDataManager = LoginDataManager.getInstance(getContext());
        if (!FingerprintUtil.isEnable(getContext())) {
            llIdTouch.setVisibility(View.GONE);
        } else {
            boolean isFingerEnable = (boolean) SPUtils.get(getContext(), "touch_id", aSwitch.isChecked());
            aSwitch.setCheckedImmediately(isFingerEnable);
        }
        btnLogin.setText(loginDataManager.isLogin() ? getResources().getString(R.string.third_part_logout) : getResources()
                .getString(R.string.third_part_login));
        btnLogin.setOnClickListener(view -> {
            if (loginDataManager.isLogin()) {
                confirmThirdLogin.show();
            } else {
                getOperation().addParameter("skip", "skip");
                getOperation().forward(ThirdLoginActivity.class);
            }
        });
        if (confirmThirdLogin == null) {
            confirmThirdLogin = new AlertDialog.Builder(getContext());
            confirmThirdLogin.setPositiveButton(getResources().getString(R.string.confirm), (dialogInterface, i0) -> {
                loginDataManager.logout(new Callback<AppResponseWrapper<String>>() {
                    @Override
                    public void onResponse(Call<AppResponseWrapper<String>> call, Response<AppResponseWrapper<String>> response) {
                        AppResponseWrapper<String> wrapper = response.body();
                        if (wrapper != null && wrapper.getSuccess()) {
                            loginDataManager.logoutSuccess();
                            RxToast.success(getResources().getString(R.string.third_logout_success));
                            btnLogin.setText(getResources().getString(R.string.third_part_login));
                        } else {
                            RxToast.error(getResources().getString(R.string.third_logout_error));
                        }
                    }

                    @Override
                    public void onFailure(Call<AppResponseWrapper<String>> call, Throwable t) {
                        RxToast.error(getResources().getString(R.string.third_logout_error));
                    }
                });
            });
            confirmThirdLogin.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });
            confirmThirdLogin.setMessage(getResources().getString(R.string.logout_hint));
            confirmThirdLogin.setTitle(getResources().getString(R.string.hint));
        }
        appVersion.setText(AndroidUtils.getVersionName(getContext()));
        if (!UpgradeUtil.getNewVersion(getContext()).isEmpty()) {
            newVerson.setVisibility(View.VISIBLE);
            llAppVersion.setOnClickListener(view -> UpgradeUtil.showUpdateHint(getContext(), true));
        }
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && btnLogin != null) {
            btnLogin.setText(loginDataManager.isLogin() ? getResources().getString(R.string.third_part_logout) : getResources()
                    .getString(R.string.third_part_login));
        }
    }

    @OnClick({R.id.ll_flutter, R.id.ll_share, R.id.ll_manager_wallet, R.id.ll_money_type, R.id.ll_language, R.id.ll_id_touch, R.id.ll_contract_version, R.id.ll_lrc_proportion, R.id.ll_margin_split, R.id.ll_app_version})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_flutter:  // Flutter
                View flutterView = Flutter.createView(
                        getActivity(),
                        getLifecycle(),
                        "/"
                );
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels-60);
                layout.topMargin = 60;
                getActivity().addContentView(flutterView, layout);
                break;
            case R.id.ll_share:  //分享
                getOperation().forward(ShareActivity.class);
                break;
            case R.id.ll_manager_wallet:  //管理钱包
                getOperation().forward(ManageWalletActivity.class);
                break;
            case R.id.ll_money_type://货币
                getOperation().forward(CurrencyActivity.class);
                break;
            case R.id.ll_language://语言
                startActivity(new Intent(mainActivity, LanguageActivity.class));
                getOperation().forward(LanguageActivity.class);
                break;
            case R.id.ll_id_touch://触控id
                aSwitch.toggle();
                SPUtils.put(getContext(), "touch_id", aSwitch.isChecked());
                break;
            case R.id.ll_contract_version://合约版本
                getOperation().forward(ContractVersionActivity.class);
                break;
            case R.id.ll_lrc_proportion://LRC费用比例
                getOperation().forward(LRCFeeRatioActivity.class);
                break;
            case R.id.ll_margin_split:// 差价分成
                getOperation().forward(MarginSplitActivity.class);
                break;
            case R.id.ll_app_version:// app版本
                break;
        }
    }
}
