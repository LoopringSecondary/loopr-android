/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:23 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.market;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import android.support.design.widget.TabLayout;
import androidx.core.app.Fragment;
import androidx.core.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.fragment.market.MarketDepthFragment;
import leaf.prod.app.fragment.market.MarketHistoryFragment;
import leaf.prod.app.presenter.market.MarketDetailPresenter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.model.TradingPair;
import leaf.prod.walletsdk.model.Trend;
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

    @BindView(R.id.kchart)
    public CandleStickChart kLineChart;

    @BindView(R.id.bchart)
    public CandleStickChart barChart;

    private List<Fragment> fragments;

    private MarketOrderDataManager orderDataManager;

    private MarketPriceDataManager priceDataManager;

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
        title.setDropdownImageButton(R.mipmap.icon_dropdown, button -> {
            finish();
            getOperation().forwardUp(MarketSelectActivity.class);
        });
        title.setRightImageButton(R.mipmap.icon_order_history, button -> getOperation().forward(MarketRecordsActivity.class));
    }

    @Override
    public void initView() {
        String[] titles = new String[2];
        titles[0] = getString(R.string.order_book);
        titles[1] = getString(R.string.dealt_order);
        fragments = new ArrayList<>();
        fragments.add(0, new MarketDepthFragment());
        fragments.add(1, new MarketHistoryFragment());
        setupViewPager(titles);
        setupButtons();

        barChart.setViewPortOffsets(100f, 0f, 0f, 0f);
        kLineChart.setViewPortOffsets(100f, 100f, 0f, 0f);
    }

    private float getMinimum(CandleStickChart chart) {
        float result = 0f;
        if (chart == kLineChart) {
            result = getLowestPrice();
        }
        return result;
    }

    private float getMaximum(CandleStickChart chart) {
        float result;
        if (chart == kLineChart) {
            result = getHighestPrice();
        } else {
            result = getMaximumVolume();
        }
        return result;
    }

    private float getHighestPrice() {
        Double result = Double.MIN_VALUE;
        List<Trend> trends = priceDataManager.getTrends();
        if (trends != null && trends.size() != 0) {
            for (Trend trend : trends) {
                if (trend.getHigh() > result) {
                    result = trend.getHigh();
                }
            }
        }
        return result.floatValue() * 1.2f;
    }

    private float getLowestPrice() {
        Double result = Double.MAX_VALUE;
        List<Trend> trends = priceDataManager.getTrends();
        if (trends != null && trends.size() != 0) {
            for (Trend trend : trends) {
                if (trend.getLow() < result) {
                    result = trend.getLow();
                }
            }
        }
        return result.floatValue() * 0.8f;
    }

    private float getMaximumVolume() {
        Double result = Double.MIN_VALUE;
        List<Trend> trends = priceDataManager.getTrends();
        if (trends != null && trends.size() != 0) {
            for (Trend trend : trends) {
                if (trend.getVol() > result) {
                    result = trend.getVol();
                }
            }
        }
        return result.floatValue() * 1.2f;
    }

    private void setupButtons() {
        buyButton.setText(getString(R.string.buy_token, orderDataManager.getTokenA()));
        sellButton.setText(getString(R.string.sell_token, orderDataManager.getTokenA()));
    }

    private void setupViewPager(String[] titles) {
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), fragments, titles));
        tradeTab.setupWithViewPager(viewPager);
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

    @Override
    public void initData() {
    }

    private void updateChartLabel(Trend trend) {
        tvOpen.setText(NumberUtils.format1(trend.getOpen(), 8));
        tvClose.setText(NumberUtils.format1(trend.getClose(), 8));
        tvHigh.setText(NumberUtils.format1(trend.getHigh(), 8));
        tvLow.setText(NumberUtils.format1(trend.getLow(), 8));
        tvVolume.setText(NumberUtils.format1(trend.getVol(), 2) + " ETH");
        tvChange.setText(trend.getChange());
        if (trend.getChange().contains("↑")) {
            tvChange.setTextColor(getResources().getColor(R.color.colorRed));
        } else {
            tvChange.setTextColor(getResources().getColor(R.color.colorGreen));
        }
    }

    private void updateTitleLabel() {
        TradingPair pair = TradingPair.builder()
                .tokenA(orderDataManager.getTokenA())
                .tokenB(orderDataManager.getTokenB())
                .description(orderDataManager.getTradePair())
                .build();
        Ticker ticker = priceDataManager.getTickersBy(pair);
        if (ticker.getChange().contains("↑")) {
            tvMarketBalance.setTextColor(getResources().getColor(R.color.colorRed));
        } else {
            tvMarketBalance.setTextColor(getResources().getColor(R.color.colorGreen));
        }
        tv24Change.setText(ticker.getChange());
        tv24Volume.setText(NumberUtils.numberformat2(ticker.getVol()) + " ETH");
        tvMarketBalance.setText(ticker.getBalanceShown() + " " + orderDataManager.getTokenB() + " ≈ " + ticker.getCurrencyShown());
    }

    private void setupChart(CandleStickChart chart, CandleData data) {
        chart.setMinOffset(0);
        chart.getXAxis().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getDescription().setEnabled(false);

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.enableGridDashedLine(10f, 10f, 0f);
        axisLeft.setGridColor(getResources().getColor(R.color.colorFortyWhite));
        axisLeft.setGridLineWidth(0.5f);
        axisLeft.setAxisMaximum(getMaximum(chart));
        axisLeft.setAxisMinimum(getMinimum(chart));
        axisLeft.setTextColor(getResources().getColor(R.color.colorFortyWhite));
        axisLeft.setDrawGridLines(true);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setDrawLabels(true);
        axisLeft.setLabelCount(3);

        chart.setData(data);
        chart.invalidate();
        chart.setNoDataText("没有数据");
        chart.setNoDataTextColor(Color.WHITE);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                llCandle.setVisibility(View.VISIBLE);
                llMarket.setVisibility(View.GONE);
                kLineChart.highlightValue(h.getX(), h.getDataSetIndex(), false);
                barChart.highlightValue(h.getX(), h.getDataSetIndex(), false);
                Trend trend = priceDataManager.getTrends().get((int) h.getX());
                updateChartLabel(trend);
            }

            @Override
            public void onNothingSelected() {
                llCandle.setVisibility(View.GONE);
                llMarket.setVisibility(View.VISIBLE);
                kLineChart.highlightValues(null);
                barChart.highlightValues(null);
            }
        });
    }

    private void updateChart(CandleStickChart chart, ArrayList<CandleEntry> values) {
        CandleDataSet set1 = new CandleDataSet(values, "Data Set");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setDrawIcons(false);
        set1.setShadowColorSameAsCandle(true);
        set1.setShadowWidth(0.7f);
        set1.setDrawValues(false);
        set1.setDecreasingColor(getResources().getColor(R.color.colorGreen));
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
        set1.setIncreasingColor(getResources().getColor(R.color.colorRed));
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setDrawVerticalHighlightIndicator(true);
        set1.setNeutralColor(getResources().getColor(R.color.colorGreen));
        set1.setHighLightColor(getResources().getColor(R.color.colorCenter));
        set1.setHighlightLineWidth(1f);
        CandleData data = new CandleData(set1);
        setupChart(chart, data);
    }

    private void updateKLineChart() {
        List<Trend> trends = priceDataManager.getTrends();
        ArrayList<CandleEntry> values = new ArrayList<>();
        for (int i = 0; i < trends.size(); i++) {
            Trend trend = trends.get(i);
            values.add(new CandleEntry(
                    i,
                    trend.getHigh().floatValue(),
                    trend.getLow().floatValue(),
                    trend.getOpen().floatValue(),
                    trend.getClose().floatValue()
            ));
        }
        updateChart(kLineChart, values);
    }

    private void updateBarChart() {
        List<Trend> trends = priceDataManager.getTrends();
        ArrayList<CandleEntry> values = new ArrayList<>();
        for (int i = 0; i < trends.size(); i++) {
            Trend trend = trends.get(i);
            CandleEntry entry;
            if (trend.getVol() == 0) {
                entry = new CandleEntry(i, 0, 0, 0, 0);
            } else if (trend.getChange().contains("↑")) {
                entry = new CandleEntry(i, 0, trend.getVol().floatValue(), 0, trend.getVol().floatValue());
            } else {
                entry = new CandleEntry(i, trend.getVol().floatValue(), 0, trend.getVol().floatValue(), 0);
            }
            values.add(entry);
        }
        updateChart(barChart, values);
    }

    public void updateAdapter() {
        updateTitleLabel();
        updateKLineChart();
        updateBarChart();
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

    @OnClick({R.id.btn_buy, R.id.btn_sell})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_buy:
                orderDataManager.setType(TradeType.buy);
                getOperation().forward(MarketTradeActivity.class);
                break;
            case R.id.btn_sell:
                orderDataManager.setType(TradeType.sell);
                getOperation().forward(MarketTradeActivity.class);
                break;
        }
    }
}
