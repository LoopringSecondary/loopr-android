package leaf.prod.app.activity.setting;

import java.util.List;

import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.model.Currency;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CurrencyActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.ll_cny)
    LinearLayout llCny;

    @BindView(R.id.ll_usd)
    LinearLayout llUsd;

    @BindView(R.id.iv_cny_check)
    ImageView ivCnyCheck;

    @BindView(R.id.iv_usd_check)
    ImageView ivUsdCheck;

    @BindView(R.id.cl_loading)
    ConstraintLayout clLoading;

    private LoopringService loopringService;

    private MarketcapDataManager marketcapDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_currency);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.set_money_type));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        if (SPUtils.get(this, "coin", "CNY").equals("CNY")) {
            ivCnyCheck.setVisibility(View.VISIBLE);
            ivUsdCheck.setVisibility(View.GONE);
        } else {
            ivCnyCheck.setVisibility(View.GONE);
            ivUsdCheck.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initData() {
        loopringService = new LoopringService();
        marketcapDataManager = MarketcapDataManager.getInstance(this);
    }

    @OnClick({R.id.ll_cny, R.id.ll_usd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_cny:
                SPUtils.put(this, "isRecreate", true);
                CurrencyUtil.setCurrency(this, Currency.CNY);
                updateWalletList(Currency.CNY);
                ivCnyCheck.setVisibility(View.VISIBLE);
                ivUsdCheck.setVisibility(View.GONE);
                break;
            case R.id.ll_usd:
                SPUtils.put(this, "isRecreate", true);
                CurrencyUtil.setCurrency(this, Currency.USD);
                updateWalletList(Currency.USD);
                ivCnyCheck.setVisibility(View.GONE);
                ivUsdCheck.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateWalletList(Currency currency) {
        clLoading.setVisibility(View.VISIBLE);
        loopringService.getPriceQuoteByToken(currency.getText(), "ETH")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(marketcapResult -> {
                    double price = 0;
                    if (marketcapResult.getTokens() != null && marketcapResult.getTokens().size() > 0) {
                        price = marketcapResult.getTokens().get(0).getPrice();
                    }
                    List<WalletEntity> wallets = WalletUtil.getWalletList(this);
                    for (WalletEntity wallet : wallets) {
                        wallets.get(wallets.indexOf(wallet))
                                .setAmount(wallet.getAmount() / marketcapDataManager.getPriceBySymbol("ETH") * price);
                        wallets.get(wallets.indexOf(wallet))
                                .setAmountShow(CurrencyUtil.format(this, wallet.getAmount()));
                        if (wallet.getAddress().equalsIgnoreCase(WalletUtil.getCurrentAddress(this))) {
                            WalletUtil.setCurrentWallet(this, wallet);
                        }
                    }
                    WalletUtil.setWalletList(this, wallets);
                    clLoading.setVisibility(View.GONE);
                });
    }
}
