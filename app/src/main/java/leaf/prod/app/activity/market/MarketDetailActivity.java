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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.fujianlian.klinechart.KLineChartView;
import com.github.fujianlian.klinechart.draw.Status;
import com.github.mikephil.charting.charts.CombinedChart;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.adapter.market.KLineChartAdapter;
import leaf.prod.app.adapter.market.KLineEntity;
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
	//
	//    @BindView(R.id.kchart_view)
	//    public KChartView kChartView;

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

	@BindView(R.id.kLineChartView)
	public KLineChartView kLineChartView;

	@BindView(R.id.ll_time_select)
	public LinearLayout llTimeSelect;

	@BindView(R.id.ll_config)
	public LinearLayout llConfig;

	@BindView(R.id.ll_indicator_select)
	public LinearLayout llIndicatorSelect;

	@BindView(R.id.btn_macd)
	public Button btnMacd;

	@BindView(R.id.btn_kdj)
	public Button btnKdj;

	@BindView(R.id.btn_rsi)
	public Button btnRsi;

	@BindView(R.id.btn_wr)
	public Button btnWr;

	@BindView(R.id.btn_ma)
	public Button btnMa;

	@BindView(R.id.btn_boll)
	public Button btnBoll;

	@BindView(R.id.btn_time)
	public Button btnTime;

	@BindView(R.id.btn_config)
	public Button btnConfig;

	private List<Fragment> fragments;

	private List<Button> intervalButtons;

	private List<Button> indicatorButtons;

	private MarketOrderDataManager orderDataManager;

	private MarketPriceDataManager priceDataManager;

	private MarketDetailPresenter presenter;

	private TrendInterval interval = TrendInterval.ONE_DAY;

	private final static int REQUEST_MARKET_CODE = 1;

	private KLineChartAdapter kLineChartAdapter;

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
		setConfigButtons();
		setupViewPager();
		kLineChartAdapter = new KLineChartAdapter();
		kLineChartView.setAdapter(kLineChartAdapter);
	}

	public void updateAdapter() {
		updateTitleLabel();
		kLineChartView.justShowLoading();
		List<KLineEntity> datas = presenter.updateChartDatas(TrendInterval.ONE_DAY);
		kLineChartAdapter.addFooterData(datas);
		kLineChartAdapter.notifyDataSetChanged();
		kLineChartView.startAnimation();
		kLineChartView.refreshEnd();
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
				List<KLineEntity> datas = presenter.updateChartDatas(interval);
				kLineChartAdapter.setNewDatas(datas);
				kLineChartView.startAnimation();
				kLineChartView.refreshEnd();
				presenter.showTimeSelect(false);
			});
		}
		btn1Day.getPaint().setFakeBoldText(true);
		btn1Day.setTextColor(getResources().getColor(R.color.colorWhite));
	}

	private void setConfigButtons() {
		indicatorButtons = new ArrayList<>();
		indicatorButtons.add(btnMacd);
		indicatorButtons.add(btnKdj);
		indicatorButtons.add(btnRsi);
		indicatorButtons.add(btnWr);
		setupConfigButtonsListener();
		kLineChartView.setChildDraw(0);
	}

	private void setupConfigButtonsListener() {
		for (Button button : indicatorButtons) {
			button.setOnClickListener(view -> {
				for (Button button1 : indicatorButtons) {
					button1.getPaint().setFakeBoldText(false);
					button1.setTextColor(getResources().getColor(R.color.colorFortyWhite));
				}
				button.getPaint().setFakeBoldText(true);
				button.setTextColor(getResources().getColor(R.color.colorWhite));
				kLineChartView.setChildDraw(indicatorButtons.indexOf(button));
				presenter.showConfigSelect(false);
			});
		}
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

	@OnClick({R.id.btn_buy, R.id.btn_sell, R.id.btn_time, R.id.btn_config, R.id.btn_ma, R.id.btn_boll})
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
			case R.id.btn_time:
				presenter.showTimeSelect(llTimeSelect.getVisibility() != View.VISIBLE);
				break;
			case R.id.btn_config:
				presenter.showConfigSelect(llIndicatorSelect.getVisibility() != View.VISIBLE);
				break;
			case R.id.btn_ma:
				btnBoll.setTextColor(getResources().getColor(R.color.colorFortyWhite));
				btnBoll.getPaint().setFakeBoldText(false);
				btnMa.setTextColor(getResources().getColor(R.color.colorWhite));
				btnMa.getPaint().setFakeBoldText(true);
				kLineChartView.changeMainDrawType(Status.MA);
				presenter.showConfigSelect(false);
				break;
			case R.id.btn_boll:
				btnBoll.setTextColor(getResources().getColor(R.color.colorWhite));
				btnBoll.getPaint().setFakeBoldText(true);
				btnMa.setTextColor(getResources().getColor(R.color.colorFortyWhite));
				btnMa.getPaint().setFakeBoldText(false);
				kLineChartView.changeMainDrawType(Status.BOLL);
				presenter.showConfigSelect(false);
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
