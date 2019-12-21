package leaf.prod.app.fragment.market;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketDetailActivity;
import leaf.prod.app.adapter.market.MarketSelectAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.MarketsType;
import leaf.prod.walletsdk.model.Ticker;

public class MarketSelectFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    private MarketsType marketsType;

    private MarketSelectAdapter marketAdapter;

    private MarketOrderDataManager marketManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_market_select, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        marketManager = MarketOrderDataManager.getInstance(getContext());
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        marketAdapter = new MarketSelectAdapter(R.layout.adapter_item_market_select, null);
        marketAdapter.setHeaderView(LayoutInflater.from(getContext())
                .inflate(R.layout.adapter_header_markets, recyclerView, false));
        recyclerView.setAdapter(marketAdapter);
        marketAdapter.setOnItemClickListener((adapter, view, position) -> {
            Ticker ticker = getTickers().get(position);
            marketManager.setTokenBuy(ticker.getTradingPair().getTokenB());
            marketManager.setTokenSell(ticker.getTradingPair().getTokenA());
            getActivity().finish();
            getOperation().forwardDown(MarketDetailActivity.class);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public List<Ticker> getTickers() {
        List<Ticker> result = null;
        MarketPriceDataManager manager = MarketPriceDataManager.getInstance(getContext());
        switch (this.marketsType) {
            case Favorite:
                result = manager.getFavTickers();
                break;
            case WETH:
            case LRC:
            case USDT:
            case TUSD:
                result = manager.getTickersBy(this.marketsType.name());
                break;
        }
        return result;
    }

    public void updateAdapter() {
        updateAdapter(getTickers());
    }

    public void updateAdapter(List<Ticker> tickers) {
        if (marketAdapter != null) {
            marketAdapter.setNewData(tickers);
            marketAdapter.notifyDataSetChanged();
        }
    }

    public void setMarketsType(MarketsType marketsType) {
        this.marketsType = marketsType;
    }
}
