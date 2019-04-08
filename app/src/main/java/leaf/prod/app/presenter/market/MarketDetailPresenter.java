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
import android.view.View;

import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketDetailActivity;
import leaf.prod.app.adapter.market.KLineEntity;
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
							if (interval == TrendInterval.ONE_DAY) {
								view.updateAdapter();
							}
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

	public List<KLineEntity> updateChartDatas(TrendInterval interval) {
		List<Trend> trends = priceDataManager.getTrendMap(interval);
		List<KLineEntity> datas = new ArrayList<>();
		if (trends != null) {
			for (Trend trend : trends) {
				datas.add(KLineEntity.convert(trend));
			}
		}
		MarketDataHelper.calculate(datas);
		return datas;
	}

	public void showTimeSelect(boolean flag) {
		view.llIndicatorSelect.setVisibility(View.GONE);
		view.btnConfig.setBackgroundColor(view.getResources().getColor(R.color.colorBg2));
		if (flag) {
			view.btnTime.setBackgroundColor(view.getResources().getColor(R.color.colorPrimary));
			view.llTimeSelect.setVisibility(View.VISIBLE);
		} else {
			view.btnTime.setBackgroundColor(view.getResources().getColor(R.color.colorBg2));
			view.llTimeSelect.setVisibility(View.GONE);
		}
	}

	public void showConfigSelect(boolean flag) {
		view.llTimeSelect.setVisibility(View.GONE);
		view.btnTime.setBackgroundColor(view.getResources().getColor(R.color.colorBg2));
		if (flag) {
			view.btnConfig.setBackgroundColor(view.getResources().getColor(R.color.colorPrimary));
			view.llIndicatorSelect.setVisibility(View.VISIBLE);
		} else {
			view.btnConfig.setBackgroundColor(view.getResources().getColor(R.color.colorBg2));
			view.llIndicatorSelect.setVisibility(View.GONE);
		}
	}
}
