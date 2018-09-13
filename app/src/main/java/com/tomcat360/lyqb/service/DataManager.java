package com.tomcat360.lyqb.service;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.lyqb.walletsdk.listener.MarketcapListener;
import com.lyqb.walletsdk.model.Currency;
import com.lyqb.walletsdk.model.request.param.MarketcapParam;
import com.lyqb.walletsdk.model.response.data.MarketcapResult;
import com.lyqb.walletsdk.model.response.data.Token;
import com.lyqb.walletsdk.service.LoopringService;
import com.tomcat360.lyqb.utils.CurrencyUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DataManager {

    private static DataManager dataManager;

    private List<Token> tokens;

    private MarketcapResult marketcapResult;

    private Context context;

    private List<BroadcastReceiver> broacastReceiverList = new ArrayList<>();

    private LoopringService loopringService = new LoopringService();

    private MarketcapListener marketcapListener = new MarketcapListener();

    private LocalBroadcastManager broadcastManager;

    private Observable<List<Token>> tokenObservable;

    private Observable<MarketcapResult> marketcapObservable;

    private DataManager(Context context) {
        this.context = context;
    }

    public static DataManager getInstance(Context context) {
        return getInstance(context, null);
    }

    public static DataManager getInstance(Context context, BroadcastReceiver broacastReceiver) {
        if (dataManager == null) {
            dataManager = new DataManager(context);
            dataManager.broadcastManager = LocalBroadcastManager.getInstance(context);
        }
        if (broacastReceiver != null && !dataManager.broacastReceiverList.contains(broacastReceiver)) {
            dataManager.broacastReceiverList.add(broacastReceiver);
            dataManager.broadcastManager.registerReceiver(broacastReceiver, new IntentFilter("marketcap"));
        }
        return dataManager;
    }

    public void removeBroadcast(BroadcastReceiver broadcastReceiver) {
        dataManager.broadcastManager.unregisterReceiver(broadcastReceiver);
        broacastReceiverList.remove(broadcastReceiver);
    }

    public void refreshTokens() {
        dataManager.updateTokens();
        dataManager.updateMarketcap();
    }

    // for common usage
    private void updateTokens() {
        if (this.tokenObservable == null) {
            this.tokenObservable = loopringService.getSupportedToken().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        this.tokenObservable.subscribe(tokens -> {
            for (Token token : tokens) {
                String image = String.format("icon_token_%s", token.getSymbol());
                int identifier = context.getResources().getIdentifier(image, "mipmap", "android");
                token.setImageResId(identifier);
            }
            this.tokens = tokens;
        });
    }

    private void updateMarketcap() {
        if (this.marketcapObservable == null) {
            this.marketcapObservable = marketcapListener.start()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            this.marketcapObservable.subscribe(marketcapResult -> {
                this.marketcapResult = marketcapResult;
                //                    broadcastManager.sendBroadcast(new Intent("marketcap"));
            });
        }
        Currency currency = CurrencyUtil.getCurrency(context);
        marketcapListener.send(MarketcapParam.builder().currency(currency.name()).build());
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public MarketcapResult getMarketcapResult() {
        return marketcapResult;
    }

    public Observable<List<Token>> getTokenObservable() {
        return tokenObservable;
    }

    public Observable<MarketcapResult> getMarketcapObservable() {
        return marketcapObservable;
    }
}