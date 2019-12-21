package leaf.prod.app.fragment.market;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketDetailActivity;
import leaf.prod.app.adapter.market.MarketsAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.presenter.market.MarketFragmentPresenter;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.MarketsType;
import leaf.prod.walletsdk.model.Ticker;

public class MarketsFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.refresh_layout)
    public RefreshLayout refreshLayout;

    private MarketsAdapter marketAdapter;

    private MarketsType marketsType;

    private MarketFragmentPresenter presenter;

    private MarketOrderDataManager orderManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_markets, container, false);
        unbinder = ButterKnife.bind(this, layout);
        refreshLayout.setOnRefreshListener(refreshLayout -> presenter.refreshTickers());
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        this.presenter = new MarketFragmentPresenter(this, getContext());
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        orderManager = MarketOrderDataManager.getInstance(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        marketAdapter = new MarketsAdapter(R.layout.adapter_item_markets, null, this);
        marketAdapter.setHeaderView(LayoutInflater.from(getContext())
                .inflate(R.layout.adapter_header_markets, recyclerView, false));
        recyclerView.setAdapter(marketAdapter);
        marketAdapter.setOnItemClickListener((adapter, view, position) -> {
            Ticker ticker = getTickers().get(position);
            orderManager.setTokenSell(ticker.getTradingPair().getTokenA());
            orderManager.setTokenBuy(ticker.getTradingPair().getTokenB());
            getOperation().forward(MarketDetailActivity.class);
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
