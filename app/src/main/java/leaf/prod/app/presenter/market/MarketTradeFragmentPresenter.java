/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 5:42 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter.market;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.vondear.rxtool.view.RxToast;
import com.xw.repo.BubbleSeekBar;

import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketErrorActivity;
import leaf.prod.app.activity.market.MarketSuccessActivity;
import leaf.prod.app.activity.trade.P2PErrorActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.market.MarketDepthAdapter;
import leaf.prod.app.fragment.market.MarketTradeFragment;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.utils.PasswordDialogUtil;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.Language;
import leaf.prod.walletsdk.model.MarketDepthItem;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.OriginOrder;
import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.model.TradingPair;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.LanguageUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.StringUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MarketTradeFragmentPresenter extends BasePresenter<MarketTradeFragment> {

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private TokenDataManager tokenDataManager;

    private MarketOrderDataManager orderDataManager;

    private MarketPriceDataManager priceDataManager;

    private OptionsPickerView datePickerView;

    private AlertDialog marketTradeDialog;

    private View marketTradeDialogView;

    private AlertDialog marketPriceDialog;

    private View marketPriceDialogView;

    private Animation shakeAnimation;

    private boolean moneyAmountChange = false;

    private TradeType tradeType;

    private double maxTradeAmount = 0;

    private Map<String, NoDataAdapter> emptyAdapters;

    private Map<String, MarketDepthAdapter> adapters;

    private Map<String, RecyclerView> recyclerViews;

    public MarketTradeFragmentPresenter(MarketTradeFragment view, Context context, TradeType type) {
        super(view, context);
        ButterKnife.bind(this, Objects.requireNonNull(view.getView()));
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        orderDataManager = MarketOrderDataManager.getInstance(context);
        priceDataManager = MarketPriceDataManager.getInstance(context);
        shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_x);
        tradeType = type;
        adapters = new HashMap<>();
        recyclerViews = new HashMap<>();
        emptyAdapters = new HashMap<>();
        initTokens();
    }

    @SuppressLint("SetTextI18n")
    public void initTokens() {
        view.tvSellTokenSymbol.setText(orderDataManager.getTokenB());
        view.tvBuyTokenSymbol.setText(orderDataManager.getTokenA());
        setInterval((int) SPUtils.get(context, "time_to_live", 1));
        setHint(0);
    }

    /**
     * 金额拖动条
     */
    public void setSeekbar(int progress) {
        view.seekBar.setProgress(progress);
        view.seekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "25%");
            array.put(2, "50%");
            array.put(3, "75%");
            array.put(4, "100%");
            return array;
        });
        if (tradeType == TradeType.buy) {
            String priceStr = view.tradePrice.getText().toString();
            view.seekBar.setEnabled(!priceStr.isEmpty() && !priceStr.equals(".") && Double.parseDouble(priceStr) > 0);
        }
        view.seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                if (!view.seekBar.isEnabled()) {
                    setHint(1);
                    return;
                }
                if (moneyAmountChange) {
                    moneyAmountChange = false;
                    return;
                }
                BalanceResult.Asset asset = balanceDataManager.getAssetBySymbol(orderDataManager.getTokenSell());
                if (tradeType == TradeType.buy) {
                    double tradePrice = Double.parseDouble(view.tradePrice.getText().toString());
                    maxTradeAmount = asset.getValue() / tradePrice;
                    view.tradeAmount.setText(NumberUtils.format6(progressFloat / 100 * maxTradeAmount, 0, 8));
                } else {
                    view.tradeAmount.setText(NumberUtils.format6(progressFloat / 100 * asset.getValue(), 0, 8));
                }
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }
        });
    }

    public void setupPriceListener() {
        view.tradePrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString();
                view.seekBar.setEnabled(false);
                if (StringUtils.isEmpty(value)) {
                    setHint(0);
                } else if (value.equals(".") || 0d == Double.parseDouble(value)) {
                    setHint(1);
                } else {
                    setHint(2);
                    view.seekBar.setEnabled(true);
                }
            }
        });
    }

    public void setupAmountListener() {
        view.tradeAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                moneyAmountChange = true;
                String value = editable.toString();
                if (StringUtils.isEmpty(value)) {
                    setHint(3);
                    view.seekBar.setProgress(0);
                } else if (value.equals(".") || 0d == Double.parseDouble(value)) {
                    setHint(4);
                    view.seekBar.setProgress(0);
                } else {
                    setHint(5);
                    BalanceResult.Asset asset = balanceDataManager.getAssetBySymbol(orderDataManager.getTokenSell());
                    double tradeAmount = Double.parseDouble(value);
                    if (tradeType == TradeType.buy) {
                        double tradePrice = Double.parseDouble(view.tradePrice.getText().toString());
                        maxTradeAmount = asset.getValue() / tradePrice;
                        if (tradeAmount > maxTradeAmount) {
                            view.seekBar.setProgress(100);
                        } else {
                            view.seekBar.setProgress((float) (maxTradeAmount != 0 ? tradeAmount / maxTradeAmount * 100 : 0));
                        }
                    } else {
                        if (asset.getValue() >= tradeAmount) {
                            view.seekBar.setProgress((float) (asset.getValue() != 0 ? tradeAmount / asset.getValue() * 100 : 0));
                        } else {
                            view.seekBar.setProgress(100);
                        }
                    }
                }
            }
        });
    }

    public void setInterval(int hours) {
        if (hours != 0) {
            SPUtils.put(context, "time_to_live", hours);
        }
        view.oneHourView.setTextColor(view.getResources().getColor(R.color.colorNineText));
        view.oneDayView.setTextColor(view.getResources().getColor(R.color.colorNineText));
        view.oneMonthView.setTextColor(view.getResources().getColor(R.color.colorNineText));
        view.customView.setTextColor(view.getResources().getColor(R.color.colorNineText));
        view.oneHourView.setTypeface(null, Typeface.NORMAL);
        view.oneDayView.setTypeface(null, Typeface.NORMAL);
        view.oneMonthView.setTypeface(null, Typeface.NORMAL);
        view.customView.setTypeface(null, Typeface.NORMAL);
        switch (hours) {
            case 1:
                view.oneHourView.setTextColor(view.getResources().getColor(R.color.colorWhite));
                view.oneHourView.setTypeface(null, Typeface.BOLD);
                break;
            case 24:
                view.oneDayView.setTextColor(view.getResources().getColor(R.color.colorWhite));
                view.oneDayView.setTypeface(null, Typeface.BOLD);
                break;
            case 24 * 30:
                view.oneMonthView.setTextColor(view.getResources().getColor(R.color.colorWhite));
                view.oneMonthView.setTypeface(null, Typeface.BOLD);
                break;
            default:
                view.customView.setTextColor(view.getResources().getColor(R.color.colorWhite));
                view.customView.setTypeface(null, Typeface.BOLD);
                List<Integer> dates = new ArrayList<>();
                List<String> dateType = Arrays.asList(view.getResources().getString(R.string.hour, ""),
                        view.getResources().getString(R.string.day, ""),
                        view.getResources().getString(R.string.month, ""));
                for (int i = 1; i <= 24; ++i) {
                    dates.add(i);
                }
                if (datePickerView == null) {
                    datePickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> {
                    }).setBgColor(view.getResources().getColor(R.color.colorTitleBac))
                            .setTitleBgColor(view.getResources().getColor(R.color.colorTitleBac))
                            .setTitleText(view.getResources().getString(R.string.expiry_date))
                            .setTitleColor(view.getResources().getColor(R.color.colorNineText))
                            .setDividerColor(view.getResources().getColor(R.color.colorBg))
                            .setTextColorCenter(view.getResources().getColor(R.color.colorWhite))
                            .setCancelText(" ")
                            .setTitleSize(14)
                            .setSubCalSize(12)
                            .setContentTextSize(15)
                            .setLineSpacingMultiplier(2)
                            .setOptionsSelectChangeListener((options1, options2, options3) -> {
                                dates.clear();
                                int amount = options2 == 0 ? 24 : (options2 == 1 ? 30 : 12);
                                for (int i = 1; i <= amount; ++i) {
                                    dates.add(i);
                                }
                                datePickerView.setNPicker(dates, dateType, null);
                                options1 = options1 > dates.size() - 1 ? 0 : options1;
                                datePickerView.setSelectOptions(options1, options2);
                                SPUtils.put(context, "time_to_live", dates.get(options1) * (options2 == 0 ? 1 : (options2 == 1 ? 24 : 24 * 30)));
                            }).build();
                    datePickerView.setNPicker(dates, dateType, null);
                }
                if (hours != 0) {
                    dates.clear();
                    if (hours / 24 <= 1) {
                        for (int i = 1; i <= 24; ++i) {
                            dates.add(i);
                        }
                        datePickerView.setNPicker(dates, dateType, null);
                        datePickerView.setSelectOptions((hours - 1) % 24, 0);
                    } else if ((hours / 24) <= 30) {
                        for (int i = 1; i <= 30; ++i) {
                            dates.add(i);
                        }
                        datePickerView.setNPicker(dates, dateType, null);
                        datePickerView.setSelectOptions(((hours / 24) - 1) % 30, 1);
                    } else {
                        for (int i = 1; i <= 12; ++i) {
                            dates.add(i);
                        }
                        datePickerView.setNPicker(dates, dateType, null);
                        datePickerView.setSelectOptions(((hours / 24 / 30) - 1) % 12, 2);
                    }
                } else {
                    datePickerView.show();
                }
                break;
        }
    }

    /**
     * 下单弹窗
     */
    @SuppressLint("SetTextI18n")
    public void showTradeDetailDialog() {
        OriginOrder order = constructOrder();
        setupTradeDialog();
        setupToken(order);
        setupPrice(order);
        setValidTime(order);
        marketTradeDialog.show();
    }

    private void setupToken(OriginOrder order) {
        String tokenBTip = view.getResources().getString(R.string.buy) + " " + order.getTokenB();
        String tokenSTip = view.getResources().getString(R.string.sell) + " " + order.getTokenS();
        int tokenBID = tokenDataManager.getTokenBySymbol(order.getTokenB()).getImageResId();
        int tokenSID = tokenDataManager.getTokenBySymbol(order.getTokenS()).getImageResId();
        if (tokenBID == 0) {
            marketTradeDialogView.findViewById(R.id.tv_token_b).setVisibility(View.VISIBLE);
            marketTradeDialogView.findViewById(R.id.iv_token_b).setVisibility(View.INVISIBLE);
            ((TextView) marketTradeDialogView.findViewById(R.id.tv_token_b)).setText(order.getTokenB());
        } else {
            marketTradeDialogView.findViewById(R.id.tv_token_b).setVisibility(View.INVISIBLE);
            marketTradeDialogView.findViewById(R.id.iv_token_b).setVisibility(View.VISIBLE);
            ((ImageView) marketTradeDialogView.findViewById(R.id.iv_token_b)).setImageResource(tokenBID);
        }
        if (tokenSID == 0) {
            marketTradeDialogView.findViewById(R.id.tv_token_s).setVisibility(View.VISIBLE);
            marketTradeDialogView.findViewById(R.id.iv_token_s).setVisibility(View.INVISIBLE);
            ((TextView) marketTradeDialogView.findViewById(R.id.tv_token_s)).setText(order.getTokenS());
        } else {
            marketTradeDialogView.findViewById(R.id.tv_token_s).setVisibility(View.INVISIBLE);
            marketTradeDialogView.findViewById(R.id.iv_token_s).setVisibility(View.VISIBLE);
            ((ImageView) marketTradeDialogView.findViewById(R.id.iv_token_s)).setImageResource(tokenSID);
        }
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_buy_token)).setText(tokenBTip);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_sell_token)).setText(tokenSTip);
    }

    private void setupPrice(OriginOrder order) {
        String amountB = NumberUtils.format6(order.getAmountBuy(), 0, 6);
        String amountS = NumberUtils.format6(order.getAmountSell(), 0, 6);
        String amountBPrice = CurrencyUtil.format(context, marketcapDataManager
                .getPriceBySymbol(order.getTokenB()) * order.getAmountBuy());
        String amountSPrice = CurrencyUtil.format(context, marketcapDataManager
                .getPriceBySymbol(order.getTokenS()) * order.getAmountSell());
        String priceQuote = view.tradePrice.getText() + " " + orderDataManager.getTradePair().replace("-", "/");
        String lrcFee = NumberUtils.format1(order.getLrc(), 3) +
                " LRC ≈ " + CurrencyUtil.format(context, marketcapDataManager.getAmountBySymbol("LRC", order.getLrc()));
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_buy_amount)).setText(amountB);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_sell_amount)).setText(amountS);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_buy_price)).setText(amountBPrice);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_sell_price)).setText(amountSPrice);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_price)).setText(priceQuote);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_trading_fee)).setText(lrcFee);
    }

    private void setValidTime(OriginOrder order) {
        String validSince = sdf.format(order.getValidS() * 1000L);
        String validUntil = sdf.format(order.getValidU() * 1000L);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_live_time)).setText(validSince + " ~ " + validUntil);
    }

    private OriginOrder constructOrder() {
        Double amountBuy = view.getAmountBuy();
        Double amountSell = view.getAmountSell();
        Date now = new Date();
        Integer validS = (int) (now.getTime() / 1000);
        int time = (int) SPUtils.get(context, "time_to_live", 1);
        Integer validU = (int) (DateUtil.addDateTime(now, time).getTime() / 1000);
        return orderDataManager.constructOrder(amountBuy, amountSell, validS, validU);
    }

    private void setupTradeDialog() {
        if (marketTradeDialog == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
            marketTradeDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_trade_detail, null);
            marketTradeDialogView.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> marketTradeDialog.hide());
            marketTradeDialogView.findViewById(R.id.btn_order).setOnClickListener(view1 -> {
                if (WalletUtil.needPassword(context)) {
                    PasswordDialogUtil.showPasswordDialog(view.getActivity(), MarketTradeFragment.PASSWORD_TYPE, v -> {
                        view.showProgress(view.getResources().getString(R.string.loading_default_messsage));
                        processOrder(PasswordDialogUtil.getInputPassword());
                    });
                } else {
                    processOrder("");
                }
            });
            builder.setView(marketTradeDialogView);
            builder.setCancelable(true);
            marketTradeDialog = builder.create();
            marketTradeDialog.setCancelable(true);
            marketTradeDialog.setCanceledOnTouchOutside(true);
            marketTradeDialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    public void showTradePriceDialog() {
        setupPriceDialog();
        marketPriceDialog.show();
    }

    private void setupPriceDialog() {
        if (marketPriceDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);
            marketPriceDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_market_order, null);
            RecyclerView recyclerViewBuy = marketPriceDialogView.findViewById(R.id.recycler_view_buy);
            RecyclerView recyclerViewSell = marketPriceDialogView.findViewById(R.id.recycler_view_sell);

            recyclerViews.put("buy", recyclerViewBuy);
            recyclerViews.put("sell", recyclerViewSell);
            builder.setView(marketPriceDialogView);
            builder.setCancelable(true);

            marketPriceDialog = builder.create();
            marketPriceDialog.setCancelable(true);
            marketPriceDialog.setCanceledOnTouchOutside(true);
            marketPriceDialog.getWindow().setGravity(Gravity.BOTTOM);
        }
        initData();
    }

    private void initData() {
        for (Map.Entry<String, RecyclerView> item : recyclerViews.entrySet()) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            MarketDepthAdapter marketAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth, null, item.getKey());
            marketAdapter.setOnItemClickListener((adapter, view, position) -> handleClick(item, position));
            item.getValue().setAdapter(marketAdapter);
            item.getValue().setLayoutManager(layoutManager);
            setHeader(item);
            adapters.put(item.getKey(), marketAdapter);
            NoDataType type = NoDataType.getNoDataType(item.getKey());
            NoDataAdapter emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, type);
            emptyAdapters.put(item.getKey(), emptyAdapter);
        }
        setupTitle();
        updateAdapter();
    }

    private void setupTitle() {
        TextView tvLatest = marketPriceDialogView.findViewById(R.id.tv_latest_price);
        TradingPair pair = TradingPair.builder()
                .tokenA(orderDataManager.getTokenA())
                .tokenB(orderDataManager.getTokenB())
                .description(orderDataManager.getTradePair())
                .build();
        Ticker ticker = priceDataManager.getTickerBy(pair);
        tvLatest.setText(ticker.getBalanceShown() + " " + orderDataManager.getTokenB() + " ≈ " + ticker.getCurrencyShown());
    }

    private void updateAdapter() {
        for (Map.Entry<String, MarketDepthAdapter> item : adapters.entrySet()) {
            if (item != null && item.getKey() != null && item.getValue() != null) {
                List<String[]> depths = priceDataManager.getDepths(item.getKey());
                if (depths == null || depths.size() == 0) {
                    NoDataAdapter adapter = emptyAdapters.get(item.getKey());
                    recyclerViews.get(item.getKey()).setAdapter(adapter);
                    adapter.refresh();
                } else {
                    recyclerViews.get(item.getKey()).setAdapter(item.getValue());
                    item.getValue().setNewData(getMarketDepthItemList(depths));
                    item.getValue().notifyDataSetChanged();
                }
            }
        }
    }

    private void setHeader(Map.Entry<String, RecyclerView> item) {
        String priceSuffix = "", amountSuffix = "";
        if (LanguageUtil.getLanguage(context) != Language.en_US) {
            priceSuffix = "(" + orderDataManager.getTokenB() + ")";
            amountSuffix = "(" + orderDataManager.getTokenA() + ")";
        }
        if (item.getKey().equals("buy")) {
            ((TextView) marketPriceDialogView.findViewById(R.id.tv_buy_price)).setText(context.getString(R.string.buy_price) + priceSuffix);
        } else {
            ((TextView) marketPriceDialogView.findViewById(R.id.tv_sell_price)).setText(context.getString(R.string.sell_price) + priceSuffix);
        }
        ((TextView) marketPriceDialogView.findViewById(R.id.tv_buy_amount)).setText(context.getString(R.string.amount) + amountSuffix);
        ((TextView) marketPriceDialogView.findViewById(R.id.tv_sell_amount)).setText(context.getString(R.string.amount) + amountSuffix);
    }

    private void handleClick(Map.Entry<String, RecyclerView> item, int position) {
        String[] values = priceDataManager.getDepths(item.getKey()).get(position);
        if (values.length == 3 && !StringUtils.isEmpty(values[0])) {
            marketPriceDialog.hide();
            view.tradePrice.setText(values[0]);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setHint(int flag) {
        switch (flag) {
            case 0: // empty
                view.tvPriceHint.setVisibility(View.INVISIBLE);
                break;
            case 1: // 0
                view.tvPriceHint.setVisibility(View.VISIBLE);
                view.tvPriceHint.setText(view.getResources().getString(R.string.input_valid_amount));
                view.tvPriceHint.setTextColor(view.getResources().getColor(R.color.colorRed));
                view.tvPriceHint.startAnimation(shakeAnimation);
                break;
            case 2: // shuzi
                view.tvPriceHint.setVisibility(View.VISIBLE);
                view.tvPriceHint.setText("≈" + CurrencyUtil.format(context, marketcapDataManager.getPriceBySymbol(orderDataManager
                        .getTokenB()) * Double.parseDouble(view.tradePrice.getText().toString())));
                view.tvPriceHint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
            case 3: // empty
                view.tvAmountHint.setVisibility(View.INVISIBLE);
                break;
            case 4: // 0
                view.tvAmountHint.setVisibility(View.VISIBLE);
                view.tvAmountHint.setText(view.getResources().getString(R.string.input_valid_amount));
                view.tvAmountHint.setTextColor(view.getResources().getColor(R.color.colorRed));
                view.tvAmountHint.startAnimation(shakeAnimation);
                break;
            case 5: // shuzi
                view.tvAmountHint.setVisibility(View.VISIBLE);
                view.tvAmountHint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                if (tradeType == TradeType.sell) {
                    view.tvAmountHint.setText(view.getResources().getString(R.string.available_balance,
                            balanceDataManager.getAssetBySymbol(orderDataManager.getTokenA())
                                    .getValueShown()) + " " + orderDataManager.getTokenA());
                } else {
                    view.tvAmountHint.setText(view.getResources()
                            .getString(R.string.market_max_buy, NumberUtils.format6(maxTradeAmount, 0, 8) + " " + orderDataManager
                                    .getTokenBuy()));
                }
                break;
        }
    }

    public void destroyDialog() {
        if (marketTradeDialog != null) {
            marketTradeDialog.dismiss();
            PasswordDialogUtil.dismiss(MarketTradeFragment.PASSWORD_TYPE);
        }
    }

    public void processOrder(String password) {
        try {
            orderDataManager.verify(password);
        } catch (Exception e) {
            view.hideProgress();
            PasswordDialogUtil.clearPassword();
            RxToast.error(context.getResources().getString(R.string.keystore_psw_error));
            e.printStackTrace();
            return;
        }
        if (!orderDataManager.isBalanceEnough()) {
            Objects.requireNonNull(view.getActivity()).finish();
            view.getOperation().forward(P2PErrorActivity.class);
        } else {
            orderDataManager.handleInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        view.getActivity().finish();
                        if (response.getError() == null) {
                            view.getOperation().forward(MarketSuccessActivity.class);
                        } else {
                            view.getOperation().addParameter("error", response.getError().getMessage());
                            view.getOperation().forward(MarketErrorActivity.class);
                        }
                        view.hideProgress();
                    });
        }
    }

    private List<MarketDepthItem> getMarketDepthItemList(List<String[]> depths) {
        List<MarketDepthItem> list = new ArrayList<>();
        for (String[] depth : depths) {
            list.add(MarketDepthItem.builder().depths(depth).rate(0).build());
        }
        return list;
    }
}
