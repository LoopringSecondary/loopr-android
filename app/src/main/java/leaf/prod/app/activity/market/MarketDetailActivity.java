/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:23 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.market;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.tifezh.kchartlib.chart.KChartView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.adapter.market.KLineEntity;
import leaf.prod.app.adapter.market.MarketChartAdapter;
import leaf.prod.app.fragment.market.MarketDepthFragment;
import leaf.prod.app.fragment.market.MarketHistoryFragment;
import leaf.prod.app.presenter.market.MarketDetailPresenter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.model.TradingPair;
import leaf.prod.walletsdk.model.TrendInterval;
import leaf.prod.walletsdk.util.NumberUtils;

public class MarketDetailActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.market_tab)
    TabLayout tradeTab;

    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    @BindView(R.id.btn_buy)
    public Button buyButton;

    @BindView(R.id.btn_sell)
    public Button sellButton;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    @BindView(R.id.ll_market_description)
    public LinearLayout llMarket;

    @BindView(R.id.ll_candle_description)
    public LinearLayout llCandle;

    @BindView(R.id.tv_market_balance)
    public TextView tvMarketBalance;

    @BindView(R.id.tv_24_change)
    public TextView tv24Change;

    @BindView(R.id.tv_24_vol)
    public TextView tv24Volume;

    @BindView(R.id.tv_open)
    public TextView tvOpen;

    @BindView(R.id.tv_close)
    public TextView tvClose;

    @BindView(R.id.tv_high)
    public TextView tvHigh;

    @BindView(R.id.tv_low)
    public TextView tvLow;

    @BindView(R.id.tv_volume)
    public TextView tvVolume;

    @BindView(R.id.tv_change)
    public TextView tvChange;
    //
    //    @BindView(R.id.kchart)
    //    public CustomCandleChart kLineChart;
    //
    //    @BindView(R.id.bchart)
    //    public CustomCandleChart barChart;

    @BindView(R.id.kchart_view)
    public KChartView kChartView;

    @BindView(R.id.kchart)
    public CombinedChart kLineChart;

    @BindView(R.id.bchart)
    public CombinedChart barChart;

    @BindView(R.id.btn_1hr)
    public Button btn1Hr;

    @BindView(R.id.btn_2hr)
    public Button btn2Hr;

    @BindView(R.id.btn_4hr)
    public Button btn4Hr;

    @BindView(R.id.btn_1day)
    public Button btn1Day;

    @BindView(R.id.btn_1week)
    public Button btn1Week;

    @BindView(R.id.sv_main)
    public ScrollView scrollView;

    private List<Fragment> fragments;

    private List<Button> intervalButtons;

    private MarketOrderDataManager orderDataManager;

    private MarketPriceDataManager priceDataManager;

    private MarketDetailPresenter presenter;

    private TrendInterval interval = TrendInterval.ONE_DAY;

    private final static int REQUEST_MARKET_CODE = 1;

    private MarketChartAdapter chartAdapter;

    private ScaleGestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_market_detail);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        clLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initPresenter() {
        orderDataManager = MarketOrderDataManager.getInstance(this);
        priceDataManager = MarketPriceDataManager.getInstance(this);
        presenter = new MarketDetailPresenter(this, this, orderDataManager.getTradePair());
    }

    @Override
    public void initTitle() {
        title.setBTitle(orderDataManager.getTradePair());
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_order_history, button -> getOperation().forward(MarketRecordsActivity.class));
        title.setDropdownImageButton(R.mipmap.icon_dropdown, button -> getOperation().forwardUPForResult(MarketSelectActivity.class, REQUEST_MARKET_CODE));
    }

    @Override
    public void initData() {
    }

    @Override
    public void initView() {
        setupButtons();
        setupViewPager();
        barChart.setViewPortOffsets(128f, 0f, 0f, 40f);
        kLineChart.setViewPortOffsets(128f, 160f, 0f, 0f);
        chartAdapter = new MarketChartAdapter();
        kChartView.setAdapter(chartAdapter);
        kChartView.showLoading();
//        final List<KLineEntity> data = DataRequest.getALL(MarketDetailActivity.this);
//        chartAdapter.addFooterData(data);
//        kChartView.startAnimation();
//        kChartView.refreshEnd();
    }

    public void updateAdapter() {
        updateTitleLabel();
        //        presenter.updateKLineChart();
        //        presenter.updateBarChart();
        List<KLineEntity> datas = presenter.updateChartDatas();
        chartAdapter.addFooterData(datas);
        kChartView.startAnimation();
        kChartView.refreshEnd();
//        kChartView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                kChartView.draw
//            }
//        });
    }

    public void updateAdapter(int index) {
        if (index == 0) {
            MarketDepthFragment depthFragment = (MarketDepthFragment) fragments.get(0);
            if (depthFragment != null) {
                depthFragment.updateAdapter();
            }
        } else if (index == 1) {
            MarketHistoryFragment historyFragment = (MarketHistoryFragment) fragments.get(1);
            if (historyFragment != null) {
                historyFragment.updateAdapter();
            }
        }
    }

    private void setupButtons() {
        intervalButtons = new ArrayList<>();
        intervalButtons.add(0, btn1Hr);
        intervalButtons.add(1, btn2Hr);
        intervalButtons.add(2, btn4Hr);
        intervalButtons.add(3, btn1Day);
        intervalButtons.add(4, btn1Week);
        setupButtonsListener();
        buyButton.setText(getString(R.string.buy_token, orderDataManager.getTokenA()));
        sellButton.setText(getString(R.string.sell_token, orderDataManager.getTokenA()));
    }

    private void setupButtonsListener() {
        for (Button button : intervalButtons) {
            button.setOnClickListener(v -> {
                for (Button button12 : intervalButtons) {
                    button12.getPaint().setFakeBoldText(false);
                    button12.setTextColor(getResources().getColor(R.color.colorFortyWhite));
                }
                Button button1 = (Button) v;
                button1.getPaint().setFakeBoldText(true);
                button1.setTextColor(getResources().getColor(R.color.colorWhite));
                interval = TrendInterval.getByName(button1.getText().toString());
                presenter.updateKLineChart();
                presenter.updateBarChart();
            });
        }
        btn1Day.getPaint().setFakeBoldText(true);
        btn1Day.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    private void setupViewPager() {
        String[] titles = new String[2];
        titles[0] = getString(R.string.order_book);
        titles[1] = getString(R.string.dealt_order);
        fragments = new ArrayList<>();
        fragments.add(0, new MarketDepthFragment());
        fragments.add(1, new MarketHistoryFragment());
        tradeTab.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), fragments, titles));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    MarketDepthFragment fragment = (MarketDepthFragment) fragments.get(position);
                    fragment.updateAdapter();
                } else if (position == 1) {
                    MarketHistoryFragment fragment = (MarketHistoryFragment) fragments.get(position);
                    fragment.updateAdapter();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void updateTitleLabel() {
        TradingPair pair = TradingPair.builder()
                .tokenA(orderDataManager.getTokenA())
                .tokenB(orderDataManager.getTokenB())
                .description(orderDataManager.getTradePair())
                .build();
        Ticker ticker = priceDataManager.getTickerBy(pair);
        if (ticker.getChange().contains("↑")) {
            tvMarketBalance.setTextColor(getResources().getColor(R.color.colorRed));
        } else {
            tvMarketBalance.setTextColor(getResources().getColor(R.color.colorGreen));
        }
        tv24Change.setText(ticker.getChange());
        tv24Volume.setText(NumberUtils.numberformat2(ticker.getVol()) + " ETH");
        tvMarketBalance.setText(ticker.getBalanceShown() + " " + orderDataManager.getTokenB() + " ≈ " + ticker.getCurrencyShown());
    }

    @OnClick({R.id.btn_buy, R.id.btn_sell})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_buy:
                orderDataManager.setType(TradeType.buy);
                getOperation().forward(MarketTradeActivity2.class);
                break;
            case R.id.btn_sell:
                orderDataManager.setType(TradeType.sell);
                getOperation().forward(MarketTradeActivity2.class);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MARKET_CODE:
                title.setBTitle(orderDataManager.getTradePair());
                buyButton.setText(getString(R.string.buy_token, orderDataManager.getTokenA()));
                sellButton.setText(getString(R.string.sell_token, orderDataManager.getTokenA()));
                presenter = new MarketDetailPresenter(this, this, orderDataManager.getTradePair());
                break;
        }
    }
}
