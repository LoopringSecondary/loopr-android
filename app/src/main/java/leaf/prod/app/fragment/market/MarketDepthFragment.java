package leaf.prod.app.fragment.market;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketTradeActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.market.MarketDepthAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.TradeType;

public class MarketDepthFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_view_buy)
    public RecyclerView recyclerViewBuy;

    @BindView(R.id.recycler_view_sell)
    public RecyclerView recyclerViewSell;

    private MarketPriceDataManager manager;

    private MarketOrderDataManager orderDataManager;

    private Map<String, NoDataAdapter> emptyAdapters;

    private Map<String, MarketDepthAdapter> adapters;

    private Map<String, RecyclerView> recyclerViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_market_depth, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        manager = MarketPriceDataManager.getInstance(getContext());
        orderDataManager = MarketOrderDataManager.getInstance(getContext());
    }

    @Override
    protected void initView() {
        adapters = new HashMap<>();
        recyclerViews = new HashMap<>();
        emptyAdapters = new HashMap<>();
        recyclerViews.put("buy", recyclerViewBuy);
        recyclerViews.put("sell", recyclerViewSell);
    }

    @Override
    protected void initData() {
        for (Map.Entry<String, RecyclerView> item : recyclerViews.entrySet()) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            MarketDepthAdapter marketAdapter = new MarketDepthAdapter(R.layout.adapter_item_market_depth, null, item.getKey());
            marketAdapter.setOnItemClickListener((adapter, view, position) -> handleClick(item, position));
            item.getValue().setAdapter(marketAdapter);
            item.getValue().setLayoutManager(layoutManager);
            this.setHeader(marketAdapter, item);
            adapters.put(item.getKey(), marketAdapter);
            NoDataType type = NoDataType.getNoDataType(item.getKey());
            NoDataAdapter emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, type);
            emptyAdapters.put(item.getKey(), emptyAdapter);
        }
    }

    private void handleClick(Map.Entry<String, RecyclerView> item, int position) {
        String[] values = manager.getDepths(item.getKey()).get(position);
        if (item.getKey().equals("buy")) {
            orderDataManager.setType(TradeType.buy);
        } else if (item.getKey().equals("sell")) {
            orderDataManager.setType(TradeType.sell);
        }
        Objects.requireNonNull(getActivity()).finish();
        getOperation().addParameter("priceFromDepth", values[0]);
        getOperation().forward(MarketTradeActivity.class);
    }

    private void setHeader(MarketDepthAdapter marketAdapter, Map.Entry<String, RecyclerView> item) {
        View header = LayoutInflater.from(getContext())
                .inflate(R.layout.adapter_header_market_depth, item.getValue(), false);
        if (item.getKey().equals("buy")) {
            ((TextView) header.findViewById(R.id.tv_price)).setText(getString(R.string.buy_price) + "(" + orderDataManager
                    .getTokenA() + ")");
            ((TextView) header.findViewById(R.id.tv_amount)).setText(getString(R.string.amount) + "(" + orderDataManager
                    .getTokenB() + ")");
            header.setBackground(getContext().getDrawable(R.drawable.radius_left_top_bg_29));
        } else {
            ((TextView) header.findViewById(R.id.tv_price)).setText(getString(R.string.sell_price) + "(" + orderDataManager
                    .getTokenA() + ")");
            ((TextView) header.findViewById(R.id.tv_amount)).setText(getString(R.string.amount) + "(" + orderDataManager
                    .getTokenB() + ")");
            header.setBackground(getContext().getDrawable(R.drawable.radius_right_top_bg_29));
        }
        marketAdapter.setHeaderView(header);
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

    public void updateAdapter() {
        for (Map.Entry<String, MarketDepthAdapter> item : adapters.entrySet()) {
            if (item != null && item.getKey() != null && item.getValue() != null) {
                List<String[]> depths = manager.getDepths(item.getKey());
                if (depths == null || depths.size() == 0) {
                    NoDataAdapter adapter = emptyAdapters.get(item.getKey());
                    recyclerViews.get(item.getKey()).setAdapter(adapter);
                    adapter.refresh();
                } else {
                    item.getValue().setNewData(depths);
                    item.getValue().notifyDataSetChanged();
                }
            }
        }
    }
}
