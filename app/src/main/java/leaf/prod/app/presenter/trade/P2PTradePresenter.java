/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 5:42 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter.trade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.app.AlertDialog;
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
import leaf.prod.app.activity.trade.P2PErrorActivity;
import leaf.prod.app.activity.trade.P2PTradeQrActivity;
import leaf.prod.app.fragment.trade.P2PTradeFragment;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.utils.PasswordDialogUtil;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class P2PTradePresenter extends BasePresenter<P2PTradeFragment> {

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");

    private String sellPrice = "0", buyPrice = "0";

    private Date validSince;

    private Date validUntil;

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private TokenDataManager tokenDataManager;

    private P2POrderDataManager p2pOrderManager;

    private OptionsPickerView datePickerView;

    private AlertDialog p2pTradeDialog;

    private View p2pTradeDialogView;

    private Animation shakeAnimation;

    /**
     * seekbar和edittext联动标志位
     */
    private boolean moneyAmountChange = false;

    public P2PTradePresenter(P2PTradeFragment view, Context context) {
        super(view, context);
        ButterKnife.bind(this, Objects.requireNonNull(view.getView()));
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        p2pOrderManager = P2POrderDataManager.getInstance(context);
        initTokens();
        shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_x);
    }

    @SuppressLint("SetTextI18n")
    public void initTokens() {
        view.tvSellTokenSymbol.setText(p2pOrderManager.getTokenSell());
        view.tvBuyTokenSymbol.setText(p2pOrderManager.getTokenBuy());
        view.tvSellTokenSymbol2.setText(p2pOrderManager.getTokenSell());
        view.tvBuyTokenSymbol2.setText(p2pOrderManager.getTokenBuy());
        Double tokenPrice1 = marketcapDataManager.getPriceBySymbol(p2pOrderManager.getTokenSell());
        Double tokenPrice2 = marketcapDataManager.getPriceBySymbol(p2pOrderManager.getTokenBuy());
        sellPrice = NumberUtils.format1(tokenPrice2 != 0 ? tokenPrice1 / tokenPrice2 : 0, BalanceDataManager.getPrecision(p2pOrderManager
                .getTokenBuy()));
        buyPrice = NumberUtils.format1(tokenPrice1 != 0 ? tokenPrice2 / tokenPrice1 : 0, BalanceDataManager.getPrecision(p2pOrderManager
                .getTokenSell()));
        view.tvSellTokenPrice.setText(" 1 " + p2pOrderManager.getTokenSell() + " ≈ " + sellPrice + " " + p2pOrderManager
                .getTokenBuy());
        view.tvBuyTokenPrice.setText("1 " + p2pOrderManager.getTokenBuy() + " ≈ " + buyPrice + " " + p2pOrderManager.getTokenSell());
        setHint(0);
        setInterval((int) SPUtils.get(context, "time_to_live", 1));
    }

    @SuppressLint("SetTextI18n")
    public void switchToken() {
        p2pOrderManager.swapToken();
        String tPrice = sellPrice;
        view.tvSellTokenSymbol.setText(p2pOrderManager.getTokenSell());
        view.tvBuyTokenSymbol.setText(p2pOrderManager.getTokenBuy());
        view.tvSellTokenPrice.setText(" 1 " + p2pOrderManager.getTokenSell() + " ≈ " + sellPrice + " " + p2pOrderManager
                .getTokenBuy());
        view.tvBuyTokenPrice.setText("1 " + p2pOrderManager.getTokenBuy() + " ≈ " + buyPrice + " " + p2pOrderManager.getTokenSell());
        view.tvSellTokenSymbol2.setText(p2pOrderManager.getTokenSell());
        view.tvBuyTokenSymbol2.setText(p2pOrderManager.getTokenBuy());
        sellPrice = buyPrice;
        buyPrice = tPrice;
        view.sellAmount.setText("");
        view.buyAmount.setText("");
        view.seekBar.setProgress(0);
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
        view.seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                if (moneyAmountChange) {
                    moneyAmountChange = false;
                    return;
                }
                BalanceResult.Asset asset = balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenSell());
                view.sellAmount.setText(NumberUtils.format1(asset.getValue() * progressFloat / 100, asset.getPrecision()));
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }
        });
    }

    public void setSellAmount() {
        view.sellAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Double sellAmountDouble = (editable.toString().isEmpty() || editable.toString()
                        .equals(".") ? 0d : Double.valueOf(editable.toString()));
                moneyAmountChange = true;
                if (sellAmountDouble == 0) {
                    setHint(0);
                    view.seekBar.setProgress(0);
                } else if (sellAmountDouble > getMaxAmount()) {
                    setHint(1);
                    view.seekBar.setProgress(100);
                } else {
                    setHint(3);
                    double amountTotal = balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenSell()).getValue();
                    view.seekBar.setProgress((float) (amountTotal != 0 ? sellAmountDouble / amountTotal * 100 : 0));
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

    public void calMarketSell() {
        try {
            Double amountS = Double.parseDouble(view.sellAmount.getText().toString());
            Double priceS = Double.parseDouble(sellPrice);
            int precision = balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenBuy()).getPrecision();
            view.buyAmount.setText(NumberUtils.format1(amountS * priceS, precision));
        } catch (Exception e) {
            view.buyAmount.setText("0");
        }
    }

    /**
     * 下单弹窗
     */
    @SuppressLint("SetTextI18n")
    public void showTradeDetailDialog() {
        if (p2pTradeDialog == null) {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);
            p2pTradeDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_trade_detail, null);
            p2pTradeDialogView.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> p2pTradeDialog.hide());
            p2pTradeDialogView.findViewById(R.id.btn_order).setOnClickListener(view1 -> {
                if (WalletUtil.needPassword(context)) {
                    PasswordDialogUtil.showPasswordDialog(view.getActivity(), P2PTradeFragment.PASSWORD_TYPE, v -> {
                        view.showProgress(view.getResources().getString(R.string.loading_default_messsage));
                        processMaker(PasswordDialogUtil.getInputPassword());
                    });
                } else {
                    processMaker("");
                }
            });
            builder.setView(p2pTradeDialogView);
            builder.setCancelable(true);
            p2pTradeDialog = builder.create();
            p2pTradeDialog.setCancelable(true);
            p2pTradeDialog.setCanceledOnTouchOutside(true);
            p2pTradeDialog.getWindow().setGravity(Gravity.CENTER);
        }
        ((ImageView) p2pTradeDialogView.findViewById(R.id.iv_token_s)).setImageDrawable(view.getResources()
                .getDrawable(tokenDataManager.getTokenBySymbol(p2pOrderManager.getTokenSell()).getImageResId()));
        ((ImageView) p2pTradeDialogView.findViewById(R.id.iv_token_b)).setImageDrawable(view.getResources()
                .getDrawable(tokenDataManager.getTokenBySymbol(p2pOrderManager.getTokenBuy()).getImageResId()));
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_sell_token)).setText(view.getResources()
                .getString(R.string.sell) + " " + p2pOrderManager.getTokenSell());
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_buy_token)).setText(view.getResources()
                .getString(R.string.buy) + " " + p2pOrderManager.getTokenBuy());
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_sell_price)).setText("≈" + CurrencyUtil.format(context, marketcapDataManager
                .getPriceBySymbol(p2pOrderManager.getTokenSell()) * Double.parseDouble(view.sellAmount.getText()
                .toString())));
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_buy_price)).setText("≈" + CurrencyUtil.format(context, marketcapDataManager
                .getPriceBySymbol(p2pOrderManager.getTokenBuy()) * Double.parseDouble(view.buyAmount.getText()
                .toString())));
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_sell_amount)).setText(view.sellAmount.getText());
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_buy_amount)).setText(view.buyAmount.getText());
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_price)).setText(NumberUtils.format1(Double.parseDouble(view.buyAmount
                .getText().toString()) / Double.parseDouble(view.sellAmount.getText()
                .toString()), BalanceDataManager.getPrecision(p2pOrderManager.getTokenSell())) + " " + p2pOrderManager.getTokenSell() + "/" + p2pOrderManager
                .getTokenBuy());
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_trading_fee)).setText("0 LRC ≈ " + CurrencyUtil.format(context, 0));
        validSince = new Date();
        int time = (int) SPUtils.get(context, "time_to_live", 1);
        validUntil = DateUtil.addDateTime(validSince, time);
        ((TextView) p2pTradeDialogView.findViewById(R.id.tv_live_time)).setText(sdf.format(validSince) + " ~ " +
                sdf.format(validUntil));
        p2pTradeDialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void setHint(int flag) {
        switch (flag) {
            case 0:
                view.tvSellHint.setText(view.getResources().getString(R.string.available_balance,
                        balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenSell())
                                .getValueShown()) + " " + p2pOrderManager.getTokenSell());
                view.tvSellHint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
            case 1:
                view.tvSellHint.setText(view.getResources().getString(R.string.available_balance,
                        balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenSell())
                                .getValueShown()) + " " + p2pOrderManager.getTokenSell());
                view.tvSellHint.setTextColor(view.getResources().getColor(R.color.colorRed));
                view.tvSellHint.startAnimation(shakeAnimation);
                break;
            case 2:
                view.tvSellHint.setText(view.getResources().getString(R.string.input_valid_amount));
                view.tvSellHint.setTextColor(view.getResources().getColor(R.color.colorRed));
                view.tvSellHint.startAnimation(shakeAnimation);
                break;
            case 3:
                view.tvSellHint.setText("≈" + CurrencyUtil.format(context, marketcapDataManager.getPriceBySymbol(p2pOrderManager
                        .getTokenSell()) * Double.parseDouble(view.sellAmount.getText().toString())));
                view.tvSellHint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
            case 4:
                view.tvBuyHint.setVisibility(View.INVISIBLE);
                break;
            case 5:
                view.tvBuyHint.setVisibility(View.VISIBLE);
                view.tvBuyHint.setText(view.getResources().getString(R.string.input_valid_amount));
                view.tvBuyHint.setTextColor(view.getResources().getColor(R.color.colorRed));
                view.tvBuyHint.startAnimation(shakeAnimation);
                break;
            case 6:
                view.tvBuyHint.setVisibility(View.VISIBLE);
                view.tvBuyHint.setText("≈" + CurrencyUtil.format(context, marketcapDataManager.getPriceBySymbol(p2pOrderManager
                        .getTokenBuy()) * Double.parseDouble(view.buyAmount.getText().toString())));
                view.tvBuyHint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
        }
    }

    public double getMaxAmount() {
        return balanceDataManager.getAssetBySymbol(p2pOrderManager.getTokenSell()).getValue();
    }

    public void destroyDialog() {
        if (p2pTradeDialog != null) {
            p2pTradeDialog.dismiss();
            PasswordDialogUtil.dismiss(P2PTradeFragment.PASSWORD_TYPE);
        }
    }

    public void processMaker(String password) {
        Double amountBuy = Double.parseDouble(view.buyAmount.getText().toString());
        Double amountSell = Double.parseDouble(view.sellAmount.getText().toString());
        Integer validS = (int) (validSince.getTime() / 1000);
        Integer validU = (int) (validUntil.getTime() / 1000);
        Integer sellCount = Integer.parseInt(view.sellCount.getText().toString());
        p2pOrderManager.constructMaker(amountBuy, amountSell, validS, validU, sellCount);
        try {
            p2pOrderManager.verify(password);
        } catch (Exception e) {
            view.hideProgress();
            PasswordDialogUtil.clearPassword();
            RxToast.error(context.getResources().getString(R.string.keystore_psw_error));
            e.printStackTrace();
            return;
        }
        if (!p2pOrderManager.isBalanceEnough()) {
            Objects.requireNonNull(view.getActivity()).finish();
            view.getOperation().forward(P2PErrorActivity.class);
        } else {
            p2pOrderManager.handleInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        view.getActivity().finish();
                        if (response.getError() == null) {
                            view.getOperation().forward(P2PTradeQrActivity.class);
                        } else {
                            view.getOperation().addParameter("error", response.getError().getMessage());
                            view.getOperation().forward(P2PErrorActivity.class);
                        }
                        view.hideProgress();
                    });
        }
    }
}
