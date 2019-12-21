package leaf.prod.app.presenter.market;

import java.util.List;

import android.content.Context;
import androidx.core.app.Fragment;
import android.view.View;

import leaf.prod.app.activity.market.MarketsActivity;
import leaf.prod.app.fragment.market.MarketsFragment;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.Ticker;
import leaf.prod.walletsdk.model.TickerSource;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MarketActivityPresenter extends BasePresenter<MarketsActivity> {

    private List<Fragment> fragments;

    private final MarketPriceDataManager marketManager;

    public MarketActivityPresenter(MarketsActivity view, Context context) {
        super(view, context);
        marketManager = MarketPriceDataManager.getInstance(context);
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

    private void updateAdapters() {
        for (Fragment item : fragments) {
            MarketsFragment fragment = (MarketsFragment) item;
            fragment.updateAdapter();
        }
    }

    public void updateAdapter(boolean isFiltering, List<Ticker> tickers) {
        marketManager.setFiltering(isFiltering);
        if (isFiltering) {
            marketManager.setFilteredTickers(tickers);
        }
        updateAdapters();
    }

    public void refreshTickers() {
        view.clLoading.setVisibility(View.VISIBLE);
        marketManager.getLoopringService()
                .getTickers(TickerSource.coinmarketcap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Ticker>>() {
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
                    public void onNext(List<Ticker> result) {
                        marketManager.convertTickers(result);
                        updateAdapters();
                        view.clLoading.setVisibility(View.GONE);
                        unsubscribe();
                    }
                });
    }
}
