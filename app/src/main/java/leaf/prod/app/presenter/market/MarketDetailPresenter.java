/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-12-29 4:15 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter.market;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketDetailActivity;
import leaf.prod.app.adapter.market.KLineEntity;
import leaf.prod.app.layout.MyMarkerView;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.utils.MarketDataHelper;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.Depth;
import leaf.prod.walletsdk.model.OrderFill;
import leaf.prod.walletsdk.model.Trend;
import leaf.prod.walletsdk.model.TrendInterval;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MarketDetailPresenter extends BasePresenter<MarketDetailActivity> {

    private TrendInterval interval = TrendInterval.ONE_DAY;

    private String market;

    private MarketPriceDataManager priceDataManager;

    private MarketOrderDataManager orderDataManager;

    public MarketDetailPresenter(MarketDetailActivity view, Context context, String market) {
        super(view, context);
        this.market = market;
        this.priceDataManager = MarketPriceDataManager.getInstance(context);
        this.orderDataManager = MarketOrderDataManager.getInstance(context);
        this.getTrend();
        this.getDepths();
        this.getOrderFills();
    }

    private void getTrend() {
        for (TrendInterval interval : TrendInterval.values()) {
            priceDataManager.getLoopringService().getTrend(market, interval)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<Trend>>() {
                        @Override
                        public void onCompleted() {
                            view.clLoading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            view.clLoading.setVisibility(View.GONE);
                            unsubscribe();
                        }

                        @Override
                        public void onNext(List<Trend> trends) {
                            priceDataManager.convertTrend(trends);
                            view.updateAdapter();
                            view.clLoading.setVisibility(View.GONE);
                            unsubscribe();
                        }
                    });
        }
    }

    private void getDepths() {
        priceDataManager.getLoopringService().getDepths(market, 20)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Depth>() {
                    @Override
                    public void onCompleted() {
                        view.clLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.clLoading.setVisibility(View.GONE);
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Depth result) {
                        priceDataManager.convertDepths(result);
                        view.updateAdapter(0);
                        view.clLoading.setVisibility(View.GONE);
                        unsubscribe();
                    }
                });
    }

    private void getOrderFills() {
        priceDataManager.getLoopringService().getOrderFills(market, "buy")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<OrderFill>>() {
                    @Override
                    public void onCompleted() {
                        view.clLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.clLoading.setVisibility(View.GONE);
                        unsubscribe();
                    }

                    @Override
                    public void onNext(List<OrderFill> result) {
                        priceDataManager.convertOrderFills(result);
                        view.updateAdapter(1);
                        unsubscribe();
                    }
                });
    }

    public List<KLineEntity> updateChartDatas() {
        List<Trend> trends = priceDataManager.getTrendMap(interval);
        List<KLineEntity> datas = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            if (trends != null) {
                for (Trend trend : trends) {
                    datas.add(KLineEntity.convert(trend));
                }
            }
        }
        MarketDataHelper.calculate(datas);
        return datas;
    }
    //================draw chart=====================

    public void updateKLineChart() {
        List<Trend> trends = priceDataManager.getTrendMap(interval);
        ArrayList<CandleEntry> values = new ArrayList<>();
        ArrayList<Entry> ma1 = new ArrayList<>();
        for (int i = 0; i < trends.size(); i++) {
            Trend trend = trends.get(i);
            values.add(new CandleEntry(
                    i,
                    trend.getHigh().floatValue(),
                    trend.getLow().floatValue(),
                    trend.getOpen().floatValue(),
                    trend.getClose().floatValue()
            ));
            ma1.add(new Entry(i, (trend.getLow().floatValue() + trend.getHigh().floatValue()) / 2));
        }
        CombinedData combinedData = new CombinedData();
        addCandleData(combinedData, values);
        addLineData(combinedData, ma1);
        setupChart(view.kLineChart, combinedData);
    }

    public void updateBarChart() {
        List<Trend> trends = priceDataManager.getTrendMap(interval);
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
        CombinedData combinedData = new CombinedData();
        addCandleData(combinedData, values);
        setupChart(view.barChart, combinedData);
    }

    private void addCandleData(CombinedData combinedData, ArrayList<CandleEntry> values) {
        CandleDataSet set1 = new CandleDataSet(values, "Data Set");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setDrawIcons(false);
        set1.setShadowColorSameAsCandle(true);
        set1.setShadowWidth(0.7f);
        set1.setDrawValues(false);
        set1.setDecreasingColor(context.getResources().getColor(R.color.colorGreen));
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
        set1.setIncreasingColor(context.getResources().getColor(R.color.colorRed));
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setDrawVerticalHighlightIndicator(true);
        set1.setNeutralColor(context.getResources().getColor(R.color.colorGreen));
        set1.setHighLightColor(context.getResources().getColor(R.color.colorCenter));
        set1.setHighlightLineWidth(1f);
        CandleData data = new CandleData(set1);
        combinedData.setData(data);
        //        setupChart(chart, data);
    }

    private void addLineData(CombinedData combinedData, ArrayList<Entry> values) {
        LineDataSet lineDataSet = new LineDataSet(values, "");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setColor(context.getResources().getColor(R.color.colorWhite));
        lineDataSet.setDrawCircles(false);
        LineData lineData = new LineData(lineDataSet);
        combinedData.setData(lineData);
    }

    private void setupChart(CombinedChart chart, CombinedData combinedData) {
        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.enableGridDashedLine(10f, 10f, 0f);
        axisLeft.setGridColor(context.getResources().getColor(R.color.colorFortyWhite));
        axisLeft.setGridLineWidth(0.5f);
        axisLeft.setAxisMaximum(getMaximum(chart));
        axisLeft.setAxisMinimum(getMinimum(chart));
        axisLeft.setTextColor(context.getResources().getColor(R.color.colorFortyWhite));
        axisLeft.setDrawGridLines(true);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setDrawLabels(true);
        axisLeft.setLabelCount(4);
        chart.setMinOffset(0);
        chart.getXAxis().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setData(combinedData);
        chart.invalidate();
        chart.setNoDataText("没有数据");
        chart.setNoDataTextColor(Color.WHITE);
        if (chart == view.kLineChart) {
            MyMarkerView mv = new MyMarkerView(context, R.layout.custom_marker_view, priceDataManager.getTrendMap(interval));
            mv.setChartView(chart);
            chart.setMarker(mv);
        }
        //        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
        //            @Override
        //            public void onValueSelected(Entry e, Highlight h) {
        //                view.llCandle.setVisibility(View.VISIBLE);
        //                view.llMarket.setVisibility(View.GONE);
        //                view.kLineChart.highlightValue(h.getX(), h.getDataSetIndex(), false);
        //                view.barChart.highlightValue(h.getX(), h.getDataSetIndex(), false);
        //                Trend trend = priceDataManager.getTrendMap(interval).get((int) h.getX());
        ////                updateChartLabel(trend);
        //            }
        //
        //            @Override
        //            public void onNothingSelected() {
        //                view.llCandle.setVisibility(View.GONE);
        //                view.llMarket.setVisibility(View.VISIBLE);
        //                view.kLineChart.highlightValues(null);
        //                view.barChart.highlightValues(null);
        //            }
        //        });
    }

    private float getMinimum(CombinedChart chart) {
        float result = 0f;
        if (chart == view.kLineChart) {
            result = getLowestPrice();
        }
        return result;
    }

    private float getMaximum(CombinedChart chart) {
        float result;
        if (chart == view.kLineChart) {
            result = getHighestPrice();
        } else {
            result = getMaximumVolume();
        }
        return result;
    }

    private float getHighestPrice() {
        Double result = Double.MIN_VALUE;
        List<Trend> trends = priceDataManager.getTrendMap(interval);
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
        List<Trend> trends = priceDataManager.getTrendMap(interval);
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
        List<Trend> trends = priceDataManager.getTrendMap(interval);
        if (trends != null && trends.size() != 0) {
            for (Trend trend : trends) {
                if (trend.getVol() > result) {
                    result = trend.getVol();
                }
            }
        }
        return result.floatValue() * 1.2f;
    }
}
