package leaf.prod.app.activity.market;

import java.math.BigDecimal;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.market.MarketDepthAdapter;
import leaf.prod.app.presenter.market.MarketTradePresenter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.NumberUtils;

public class MarketTradeActivity2 extends BaseActivity {

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.tab_buy)
    public Button tabBuy;

    @BindView(R.id.tab_sell)
    public Button tabSell;

    @BindView(R.id.btn_buy)
    public Button btnBuy;

    @BindView(R.id.btn_sell)
    public Button btnSell;

    @BindView(R.id.et_price)
    public EditText etPrice;

    @BindView(R.id.et_amount)
    public EditText etAmount;

    @BindView(R.id.one_hour)
    public Button oneHour;

    @BindView(R.id.one_day)
    public Button oneDay;

    @BindView(R.id.one_month)
    public Button oneMonth;

    @BindView(R.id.custom)
    public Button custom;

    @BindView(R.id.btn_per25)
    public Button btnPer25;

    @BindView(R.id.btn_per50)
    public Button btnPer50;

    @BindView(R.id.btn_per75)
    public Button btnPer75;

    @BindView(R.id.btn_per100)
    public Button btnPer100;

    @BindView(R.id.tv_market_price)
    public TextView tvMarketPrice;

    @BindView(R.id.rv_sell)
    public RecyclerView rvSell;

    @BindView(R.id.rv_buy)
    public RecyclerView rvBuy;

    @BindView(R.id.tv_hint)
    public TextView tvHint;

    @BindView(R.id.tv_avail_sell)
    public TextView tvAvailSell;

    @BindView(R.id.tv_avail_buy)
    public TextView tvAvailBuy;

    @BindView(R.id.tv_buy_amount)
    public TextView tvBuyAmount;

    @BindView(R.id.ll_avail_sell)
    public LinearLayout llAvailSell;

    @BindView(R.id.ll_avail_buy)
    public LinearLayout llAvailBuy;

    @BindView(R.id.ll_buy_amount)
    public LinearLayout llBuyAmount;

    private MarketDepthAdapter sellAdapter;

    private MarketDepthAdapter buyAdapter;

    private MarketOrderDataManager orderDataManager;

    private MarketPriceDataManager priceDataManager;

    private MarketcapDataManager marketcapDataManager;

    private MarketTradePresenter presenter;

    private int precision = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_market_trade2);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
        orderDataManager = MarketOrderDataManager.getInstance(this);
        priceDataManager = MarketPriceDataManager.getInstance(this);
        marketcapDataManager = MarketcapDataManager.getInstance(this);
        presenter = new MarketTradePresenter(this, this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(orderDataManager.getTradePair());
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_order_history, button -> getOperation().forward(MarketRecordsActivity.class));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        oneHour.setText(getString(R.string.hour, "1"));
        oneDay.setText(getString(R.string.day, "1"));
        oneMonth.setText(getString(R.string.month, "1"));
        presenter.init();
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty() || ".".equals(editable.toString())) {
                    tvHint.setText("");
                } else {
                    double price = Double.parseDouble(editable.toString());
                    double currentPrice = BigDecimal.valueOf(marketcapDataManager.getPriceBySymbol(orderDataManager.getTokenSell()))
                            .multiply(BigDecimal.valueOf(price))
                            .doubleValue();
                    tvHint.setText("≈ " + CurrencyUtil.format(MarketTradeActivity2.this, currentPrice));
                }
                presenter.setBalanceHint();
            }
        });
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                presenter.setBalanceHint();
            }
        });
    }

    @Override
    public void initData() {
        List<String[]> buyList = priceDataManager.getDepths("buy").subList(0, 5);
        List<String[]> sellList = Lists.reverse(priceDataManager.getDepths("sell").subList(0, 5));
        orderDataManager.setPriceFromDepth(getIntent().getStringExtra("priceFromDepth"));
        if (buyList.size() < 5) {
            for (int i = buyList.size(); i <= 5; ++i) {
                buyList.add(null);
            }
        }
        if (sellList.size() < 5) {
            for (int i = 0; i < 5 - sellList.size(); ++i) {
                sellList.add(i, null);
            }
        }
        sellAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth_5, sellList.subList(sellList.size() - 5, sellList
                .size()), "sell");
        sellAdapter.setOnItemClickListener((adapter, view, position) -> {
            String[] sell = sellList.get(position);
            if (sell != null && !sell[0].isEmpty()) {
                presenter.clickPrice(NumberUtils.format1(Double.parseDouble(sell[0]), 8));
            }
        });
        rvSell.setAdapter(sellAdapter);
        rvSell.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        buyAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth_5, buyList, "buy");
        buyAdapter.setOnItemClickListener((adapter, view, position) -> {
            String[] buy = buyList.get(position);
            if (buy != null && !buy[0].isEmpty()) {
                presenter.clickPrice(NumberUtils.format1(Double.parseDouble(buy[0]), 8));
            }
        });
        rvBuy.setAdapter(buyAdapter);
        rvBuy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.tab_buy, R.id.tab_sell, R.id.btn_minus_price, R.id.btn_plus_price, R.id.btn_minus_amount, R.id.btn_plus_amount,
            R.id.btn_per25, R.id.btn_per50, R.id.btn_per75, R.id.btn_per100, R.id.tv_market_price, R.id.btn_buy, R.id.btn_sell})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tab_buy:
                if (!tabBuy.isSelected()) {
                    tabBuy.setSelected(true);
                    tabSell.setSelected(false);
                    btnBuy.setVisibility(tabBuy.isSelected() ? View.VISIBLE : View.GONE);
                    btnSell.setVisibility(tabSell.isSelected() ? View.VISIBLE : View.GONE);
                    orderDataManager.setType(TradeType.buy);
                    presenter.init();
                }
                break;
            case R.id.tab_sell:
                if (!tabSell.isSelected()) {
                    tabBuy.setSelected(false);
                    tabSell.setSelected(true);
                    btnBuy.setVisibility(tabBuy.isSelected() ? View.VISIBLE : View.GONE);
                    btnSell.setVisibility(tabSell.isSelected() ? View.VISIBLE : View.GONE);
                    orderDataManager.setType(TradeType.sell);
                    presenter.init();
                }
                break;
            case R.id.btn_minus_price:
                if (!etPrice.getText().toString().isEmpty()) {
                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(etPrice.getText().toString()));
                    if (price.compareTo(BigDecimal.ZERO) > 0) {
                        etPrice.setText(NumberUtils.format1(price.subtract(BigDecimal.valueOf(Math.pow(10, -1 * precision)))
                                .doubleValue(), precision));
                    }
                }
                break;
            case R.id.btn_plus_price:
                if (!etPrice.getText().toString().isEmpty()) {
                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(etPrice.getText().toString()));
                    etPrice.setText(NumberUtils.format1(price.add(BigDecimal.valueOf(Math.pow(10, -1 * precision)))
                            .doubleValue(), precision));
                }
                break;
            //            case R.id.btn_minus_amount:
            //                if (!etAmount.getText().toString().isEmpty()) {
            //                    BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(etAmount.getText().toString()));
            //                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
            //                        etAmount.setText(NumberUtils.format1(amount.subtract(BigDecimal.valueOf(Math.pow(10, -1 * sellAsset
            //                                .getPrecision()))).doubleValue(), sellAsset.getPrecision()));
            //                    }
            //                }
            //                break;
            //            case R.id.btn_plus_amount:
            //                if (!etAmount.getText().toString().isEmpty()) {
            //                    BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(etAmount.getText().toString()));
            //                    etAmount.setText(NumberUtils.format1(amount.add(BigDecimal.valueOf(Math.pow(10, -1 * sellAsset
            //                            .getPrecision()))).doubleValue(), sellAsset.getPrecision()));
            //                }
            //                break;
            case R.id.btn_per25:
                presenter.setPercent(0);
                break;
            case R.id.btn_per50:
                presenter.setPercent(1);
                break;
            case R.id.btn_per75:
                presenter.setPercent(2);
                break;
            case R.id.btn_per100:
                presenter.setPercent(3);
                break;
            case R.id.tv_market_price:
                presenter.clickMarketPrice();
                break;
            case R.id.btn_buy:
            case R.id.btn_sell:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    presenter.showTradeDetailDialog();
                }
                break;
        }
    }
}
