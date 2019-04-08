package leaf.prod.app.fragment.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import leaf.prod.walletsdk.model.Language;
import leaf.prod.walletsdk.model.MarketDepthItem;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.util.LanguageUtil;
import leaf.prod.walletsdk.util.StringUtils;

public class MarketDepthFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_view_buy)
    public RecyclerView recyclerViewBuy;

    @BindView(R.id.recycler_view_sell)
    public RecyclerView recyclerViewSell;

    @BindView(R.id.tv_buy_price)
    public TextView tvBuyPrice;

    @BindView(R.id.tv_buy_amount)
    public TextView tvBuyAmount;

    @BindView(R.id.tv_sell_price)
    public TextView tvSellPrice;

    @BindView(R.id.tv_sell_amount)
    public TextView tvSellAmount;

    private MarketPriceDataManager manager;

    private MarketOrderDataManager orderDataManager;

    private Map<String, NoDataAdapter> emptyAdapters;

    private Map<String, MarketDepthAdapter> adapters;

    private Map<String, RecyclerView> recyclerViews;

    private boolean first = true;

    private List<String[]> buyDepths, sellDepths = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    updateDepths();
                    break;
            }
        }
    };

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
            item.getValue().setNestedScrollingEnabled(false);
            this.setHeader(item);
            adapters.put(item.getKey(), marketAdapter);
            NoDataType type = NoDataType.getNoDataType(item.getKey());
            NoDataAdapter emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, type);
            emptyAdapters.put(item.getKey(), emptyAdapter);
        }
    }

    private void handleClick(Map.Entry<String, RecyclerView> item, int position) {
        String[] values = manager.getDepths(item.getKey()).get(position);
        if (values != null && !StringUtils.isEmpty(values[0])) {
            if (item.getKey().equals("buy")) {
                orderDataManager.setType(TradeType.buy);
            } else if (item.getKey().equals("sell")) {
                orderDataManager.setType(TradeType.sell);
            }
            getOperation().addParameter("priceFromDepth", values[0]);
            getOperation().forward(MarketTradeActivity.class);
        }
    }

    private void setHeader(Map.Entry<String, RecyclerView> item) {
        String priceSuffix = "", amountSuffix = "";
        if (LanguageUtil.getLanguage(getContext()) != Language.en_US) {
            priceSuffix = "(" + orderDataManager.getTokenB() + ")";
            amountSuffix = "(" + orderDataManager.getTokenA() + ")";
        }
        if (item.getKey().equals("buy")) {
            tvBuyPrice.setText(getContext().getString(R.string.buy_price) + priceSuffix);
        } else {
            tvSellPrice.setText(getContext().getString(R.string.sell_price) + priceSuffix);
        }
        tvBuyAmount.setText(getContext().getString(R.string.amount) + amountSuffix);
        tvSellAmount.setText(getContext().getString(R.string.amount) + amountSuffix);
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
        recyclerViews.get("buy").setAdapter(adapters.get("buy"));
        recyclerViews.get("sell").setAdapter(adapters.get("sell"));
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }
        }, 0, 2000);
    }

    private void updateDepths() {
        if (adapters != null) {
            double max = 0;
            if (first) {
                first = false;
                buyDepths = manager.getDepths("buy");
                sellDepths = manager.getDepths("sell");
            } else {
                buyDepths = genData(buyDepths);
                sellDepths = genData(sellDepths);
            }
            for (int i = 0; i < (buyDepths.size() > 10 ? 10 : buyDepths.size()); ++i) {
                String[] depth = buyDepths.get(i);
                if (depth != null && depth.length == 3 && !StringUtils.isEmpty(depth[1])) {
                    max = Double.valueOf(depth[1]) > max ? Double.valueOf(depth[1]) : max;
                }
            }
            for (int i = 0; i < (sellDepths.size() > 10 ? 10 : sellDepths.size()); ++i) {
                String[] depth = sellDepths.get(i);
                if (depth != null && depth.length == 3 && !StringUtils.isEmpty(depth[1])) {
                    max = Double.valueOf(depth[1]) > max ? Double.valueOf(depth[1]) : max;
                }
            }
            if (buyDepths != null && buyDepths.size() > 0) {
                MarketDepthAdapter adapter = adapters.get("buy");
                adapter.setNewData(calculateRate(buyDepths.subList(0, 10), max));
                adapter.notifyDataSetChanged();
            }
            if (sellDepths != null && sellDepths.size() > 0) {
                MarketDepthAdapter adapter = adapters.get("sell");
                adapter.setNewData(calculateRate(sellDepths.subList(0, 10), max));
                adapter.notifyDataSetChanged();
            }
            //                    for (Map.Entry<String, MarketDepthAdapter> item : adapters.entrySet()) {
            //                        if (item != null && item.getKey() != null && item.getValue() != null) {
            //                            List<String[]> depths = manager.getDepths(item.getKey());
            //                            if (depths != null && depths.size() != 0) {
            //                                for (String[] depth : depths) {
            //                                    if (depth != null && depth.length == 3 && !StringUtils.isEmpty(depth[1])) {
            //                                        max = Double.valueOf(depth[1]) > max ? Double.valueOf(depth[1]) : max;
            //                                    }
            //                                }
            //                            }
            //                        }
            //                    }
            //                    for (Map.Entry<String, MarketDepthAdapter> item : adapters.entrySet()) {
            //                        if (item != null && item.getKey() != null && item.getValue() != null) {
            //                            List<String[]> depths = manager.getDepths(item.getKey());
            //                            if (depths == null || depths.size() == 0) {
            //                                NoDataAdapter adapter = emptyAdapters.get(item.getKey());
            //                                recyclerViews.get(item.getKey()).setAdapter(adapter);
            //                                adapter.refresh();
            //                            } else {
            //                                MarketDepthAdapter adapter = item.getValue();
            //                                recyclerViews.get(item.getKey()).setAdapter(adapter);
            //                                adapter.setNewData(calculateRate(depths, max));
            //                                adapter.notifyDataSetChanged();
            //                            }
            //                        }
            //                    }
        }
    }

    private List<String[]> genData(List<String[]> list) {
        List<String[]> list1 = new ArrayList<>(list);
        int step = (int) (Math.random() * 10);
        for (int i = 0; i < step; ++i) {
            if (list1.size() > 0) {
                list1.add(0, new String[]{"0.0001", Math.random() * 10000 + "", ""});
            }
        }
        return list1;
    }

    private List<MarketDepthItem> calculateRate(List<String[]> depths, double max) {
        List<MarketDepthItem> list = new ArrayList<>();
        for (String[] depth : depths) {
            double rate = max != 0 && !StringUtils.isEmpty(depth[1]) ? Double.valueOf(depth[1]) / max : 0;
            list.add(MarketDepthItem.builder().depths(depth).rate(rate).build());
        }
        return list;
    }
}
