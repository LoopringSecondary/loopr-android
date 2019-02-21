package leaf.prod.app.presenter.market;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketErrorActivity;
import leaf.prod.app.activity.market.MarketSuccessActivity;
import leaf.prod.app.activity.market.MarketTradeActivity2;
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
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.OriginOrder;
import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.model.TradingPair;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.DpUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-02-15 4:17 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class MarketTradePresenter extends BasePresenter<MarketTradeActivity2> {

    private MarketDepthAdapter sellAdapter;

    private MarketDepthAdapter buyAdapter;

    private NoDataAdapter emptySellAdapter;

    private NoDataAdapter emptyBuyAdapter;

    private View marketTradeDialogView;

    private AlertDialog marketTradeDialog;

    private MarketOrderDataManager orderDataManager;

    private MarketPriceDataManager priceDataManager;

    private BalanceDataManager balanceDataManager;

    private TokenDataManager tokenDataManager;

    private MarketcapDataManager marketcapDataManager;

    private Ticker ticker;

    private BalanceResult.Asset assetA, assetB;

    private List<Button> buttonList = new ArrayList<>();

    private List<String[]> buyPriceList, sellPriceList;

    public MarketTradePresenter(MarketTradeActivity2 view, Context context) {
        super(view, context);
        orderDataManager = MarketOrderDataManager.getInstance(context);
        priceDataManager = MarketPriceDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        buttonList.add(view.btnPer25);
        buttonList.add(view.btnPer50);
        buttonList.add(view.btnPer75);
        buttonList.add(view.btnPer100);
        addPrecisionList(Arrays.asList(8, 4, 2));
    }

    @SuppressLint("SetTextI18n")
    public void setBalanceHint() {
        if (!view.etPrice.getText().toString().isEmpty() && !view.etPrice.getText().toString().equals(".")
                && !view.etAmount.getText().toString().isEmpty() && !view.etAmount.getText().toString().equals(".")) {
            view.llAvailSell.setVisibility(View.GONE);
            view.llAvailBuy.setVisibility(View.GONE);
            view.llBuyAmount.setVisibility(View.VISIBLE);
            if (orderDataManager.getType() == TradeType.buy) {
                view.tvBuyAmount.setText(NumberUtils.format1(BigDecimal.valueOf(Double.parseDouble(view.etPrice.getText()
                        .toString()))
                        .multiply(BigDecimal.valueOf(Double.parseDouble(view.etAmount.getText().toString())))
                        .doubleValue(), assetB.getPrecision()) + " " + assetB.getSymbol());
            } else {
                view.tvBuyAmount.setText(NumberUtils.format1(BigDecimal.valueOf(Double.parseDouble(view.etAmount.getText()
                        .toString()))
                        .divide(BigDecimal.valueOf(Double.parseDouble(view.etPrice.getText()
                                .toString())), assetA.getPrecision())
                        .doubleValue(), assetA.getPrecision()) + " " + assetA.getSymbol());
            }
            view.btnBuy.setBackground(view.getResources().getDrawable(R.drawable.button_green));
            view.btnSell.setBackground(view.getResources().getDrawable(R.drawable.button_red));
            view.btnBuy.setTextColor(view.getResources().getColor(R.color.colorWhite));
            view.btnSell.setTextColor(view.getResources().getColor(R.color.colorWhite));
        } else {
            view.llAvailBuy.setVisibility(View.VISIBLE);
            view.llAvailSell.setVisibility(View.VISIBLE);
            view.llBuyAmount.setVisibility(View.GONE);
            view.btnBuy.setBackground(view.getResources().getDrawable(R.drawable.button_grey));
            view.btnSell.setBackground(view.getResources().getDrawable(R.drawable.button_grey));
            view.btnBuy.setTextColor(view.getResources().getColor(R.color.colorNineText));
            view.btnSell.setTextColor(view.getResources().getColor(R.color.colorNineText));
        }
    }

    @SuppressLint("SetTextI18n")
    public void init() {
        TradingPair tradingPair = TradingPair.builder()
                .tokenA(orderDataManager.getTokenA())
                .tokenB(orderDataManager.getTokenB())
                .description(orderDataManager.getTradePair())
                .build();
        ticker = priceDataManager.getTickerBy(tradingPair);
        if (ticker != null) {
            view.tvMarketPrice.setText(ticker.getBalanceShown());
            view.tvMarketPriceToken.setText(" " + orderDataManager.getTokenB() + " ≈ " + ticker.getCurrencyShown());
        }
        assetA = balanceDataManager.getAssetBySymbol(orderDataManager.getTokenA());
        assetB = balanceDataManager.getAssetBySymbol(orderDataManager.getTokenB());
        if (orderDataManager.getType() == TradeType.buy) {
            view.tabBuy.setSelected(true);
            view.btnBuy.setVisibility(View.VISIBLE);
            view.tvAvailSell.setText(assetB.getValueShown() + " " + orderDataManager.getTokenB());
            view.tvAvailBuy.setText(BigDecimal.valueOf(assetB.getValue())
                    .divide(BigDecimal.valueOf(ticker.getLast()), assetA.getPrecision(), BigDecimal.ROUND_HALF_EVEN)
                    .toPlainString() + " " + orderDataManager.getTokenA());
        } else {
            view.tabSell.setSelected(true);
            view.btnSell.setVisibility(View.VISIBLE);
            view.tvAvailSell.setText(assetA.getValueShown() + " " + orderDataManager.getTokenA());
            view.tvAvailBuy.setText(NumberUtils.format1(BigDecimal.valueOf(assetA.getValue())
                    .multiply(BigDecimal.valueOf(ticker.getLast()))
                    .doubleValue(), assetB.getPrecision()) + " " + orderDataManager.getTokenB());
        }
        for (int i = 0; i < buttonList.size(); ++i) {
            if (buttonList.get(i).isSelected()) {
                setPercent(i);
                break;
            }
        }
    }

    private void addPrecisionList(List<Integer> list) {
        for (int step : list) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DpUtil.dp2Int(context, 32)));
            textView.setText(context.getResources().getString(R.string.precision, step));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            textView.setTextColor(context.getResources().getColor(R.color.colorNineText));
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(view -> {
                List<String[]> newSellList = new ArrayList<>(), newBuyList = new ArrayList<>();
                for (String[] item : sellPriceList) {
                    newSellList.add(new String[]{NumberUtils.format1(Double.parseDouble(item[0]), step), item[1], item[2]});
                }
                setSellPriceList(newSellList);
                for (String[] item : buyPriceList) {
                    newBuyList.add(new String[]{NumberUtils.format1(Double.parseDouble(item[0]), step), item[1], item[2]});
                }
                setBuyPriceList(newBuyList);
            });
            view.llPrecision.addView(textView);
        }
    }

    public void setSellPriceList(List<String[]> sellList) {
        if (sellAdapter == null) {
            this.sellPriceList = sellList;
            view.rvSell.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            if (sellPriceList.size() > 0) {
                sellAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth_5, sellPriceList, "sell");
                sellAdapter.setOnItemClickListener((adapter, view, position) -> {
                    String[] sell = sellPriceList.get(position);
                    if (sell != null && !sell[0].isEmpty()) {
                        clickPrice(NumberUtils.format1(Double.parseDouble(sell[0]), 8));
                    }
                });
                view.rvSell.setAdapter(sellAdapter);
            } else {
                emptySellAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.market_depth_sell);
                view.rvSell.setAdapter(emptySellAdapter);
                emptySellAdapter.refresh();
            }
        } else {
            if (sellList.size() > 0) {
                for(String[] item : sellList) {
                    Log.d("", item[0] + " " + item[1] + " " + item[2]);
                }
                sellAdapter.setNewData(sellList);
            }
        }
    }

    public void setBuyPriceList(List<String[]> buyList) {
        if (buyAdapter == null) {
            this.buyPriceList = buyList;
            view.rvBuy.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            if (buyPriceList.size() > 0) {
                buyAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth_5, buyPriceList, "buy");
                buyAdapter.setOnItemClickListener((adapter, view, position) -> {
                    String[] buy = buyPriceList.get(position);
                    if (buy != null && !buy[0].isEmpty()) {
                        clickPrice(NumberUtils.format1(Double.parseDouble(buy[0]), 8));
                    }
                });
                view.rvBuy.setAdapter(buyAdapter);
            } else {
                emptyBuyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.market_depth_buy);
                view.rvBuy.setAdapter(emptyBuyAdapter);
                emptyBuyAdapter.refresh();
            }
        } else {
            if (buyList.size() > 0) {
                buyAdapter.setNewData(buyList);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void clickMarketPrice() {
        if (ticker != null) {
            clickPrice(ticker.getBalanceShown());
        }
    }

    @SuppressLint("SetTextI18n")
    public void setPercent(int pos) {
        if (!view.etPrice.getText().toString().isEmpty() && !view.etPrice.getText().toString().equals(".")) {
            for (int i = 0; i < buttonList.size(); ++i) {
                if (i == pos) {
                    buttonList.get(i).setSelected(true);
                    if (orderDataManager.getType() == TradeType.buy) {
                        view.etAmount.setText("" + NumberUtils.format1(BigDecimal.valueOf(assetB.getValue())
                                .divide(BigDecimal.valueOf(Double.parseDouble(view.etPrice.getText()
                                        .toString())), assetB.getPrecision())
                                .doubleValue() * (0.25 * (i + 1)), assetB.getPrecision()));
                        view.tvBuyAmount.setText(NumberUtils.format1(BigDecimal.valueOf(Double.parseDouble(view.etPrice.getText()
                                .toString()))
                                .multiply(BigDecimal.valueOf(Double.parseDouble(view.etAmount.getText().toString())))
                                .doubleValue(), assetB.getPrecision()) + " " + assetB.getSymbol());
                    } else {
                        view.etAmount.setText(NumberUtils.format1(assetA.getValue() * (0.25 * (i + 1)), assetA.getPrecision()));
                        view.tvBuyAmount.setText(NumberUtils.format1(BigDecimal.valueOf(assetA.getValue() * (0.25 * (i + 1)))
                                .multiply(BigDecimal.valueOf(Double.parseDouble(view.etPrice.getText().toString())))
                                .doubleValue(), assetB.getPrecision()) + " " + assetB.getSymbol());
                    }
                } else {
                    buttonList.get(i).setSelected(false);
                }
            }
        }
    }

    public void clickPrice(String price) {
        view.etPrice.setText(price);
        for (int i = 0; i < buttonList.size(); ++i) {
            if (buttonList.get(i).isSelected()) {
                setPercent(i);
                break;
            }
        }
    }

    public void doBuyOrSell() {
        double amount = (view.etAmount.getText().toString().isEmpty() || view.etAmount.getText()
                .toString().equals(".") ? 0d : Double.valueOf(view.etAmount.getText().toString()));
        double price = (view.etPrice.getText().toString().isEmpty() || view.etPrice.getText()
                .toString().equals(".") ? 0d : Double.valueOf(view.etPrice.getText().toString()));
        if (price != 0 && amount != 0) {
            //            MyViewUtils.hideInput(view);
            showTradeDetailDialog();
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
        //        setValidTime(order);
        marketTradeDialog.show();
    }

    private OriginOrder constructOrder() {
        Double amountBuy = orderDataManager.getType() == TradeType.buy ? Double.parseDouble(view.etAmount.getText()
                .toString()) : Double.parseDouble(view.etAmount.getText()
                .toString()) * Double.parseDouble(view.etPrice.getText().toString());
        Double amountSell = orderDataManager.getType() == TradeType.buy ? Double.parseDouble(view.etAmount.getText()
                .toString()) * Double.parseDouble(view.etPrice.getText()
                .toString()) : Double.parseDouble(view.etAmount.getText().toString());
        Date now = new Date();
        Integer validS = (int) (now.getTime() / 1000);
        int time = (int) SPUtils.get(context, "time_to_live", 1);
        Integer validU = (int) (DateUtil.addDateTime(now, time).getTime() / 1000);
        return orderDataManager.constructOrder(amountBuy, amountSell, validS, validU);
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
        String priceQuote = view.etPrice.getText() + " " + orderDataManager.getTradePair().replace("-", "/");
        String lrcFee = NumberUtils.format1(order.getLrc(), 3) +
                " LRC ≈ " + CurrencyUtil.format(context, marketcapDataManager.getAmountBySymbol("LRC", order.getLrc()));
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_buy_amount)).setText(amountB);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_sell_amount)).setText(amountS);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_buy_price)).setText(amountBPrice);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_sell_price)).setText(amountSPrice);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_price)).setText(priceQuote);
        ((TextView) marketTradeDialogView.findViewById(R.id.tv_trading_fee)).setText(lrcFee);
    }
    //    private void setValidTime(OriginOrder order) {
    //        String validSince = sdf.format(order.getValidS() * 1000L);
    //        String validUntil = sdf.format(order.getValidU() * 1000L);
    //        ((TextView) marketTradeDialogView.findViewById(R.id.tv_live_time)).setText(validSince + " ~ " + validUntil);
    //    }

    private void setupTradeDialog() {
        if (marketTradeDialog == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
            marketTradeDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_trade_detail, null);
            marketTradeDialogView.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> marketTradeDialog.hide());
            marketTradeDialogView.findViewById(R.id.btn_order).setOnClickListener(view1 -> {
                if (WalletUtil.needPassword(context)) {
                    PasswordDialogUtil.showPasswordDialog(view, MarketTradeFragment.PASSWORD_TYPE, v -> {
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

    private void processOrder(String password) {
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
            Objects.requireNonNull(view).finish();
            view.getOperation().forward(P2PErrorActivity.class);
        } else {
            orderDataManager.handleInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        view.finish();
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
}
