package leaf.prod.app.activity.market;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.trade.P2PRecordDetailActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.market.MarketRecordAdapter;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.Order;
import leaf.prod.walletsdk.model.OrderType;
import leaf.prod.walletsdk.model.response.relay.PageWrapper;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MarketRecordsActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.et_search)
    EditText etSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.cancel_text)
    TextView cancelText;

    @BindView(R.id.ll_search)
    LinearLayout llSearch;

    @BindView(R.id.left_btn1)
    ImageView leftBtn1;

    @BindView(R.id.refresh_layout)
    RefreshLayout refreshLayout;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    private MarketRecordAdapter recordAdapter;

    private NoDataAdapter emptyAdapter;

    private List<Order> orderList = new ArrayList<>();

    private List<Order> searchList = new ArrayList<>();

    private MarketOrderDataManager marketManager;

    private boolean isFiltering = false;

    private int currentPageIndex = 1, pageSize = 20, totalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_market_records);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        clLoading.setVisibility(View.VISIBLE);
        refreshLayout.setOnRefreshListener(refreshLayout -> refreshOrders(1));
        isFiltering = false;
        refreshOrders(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.hideSearch();
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.orders));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_search, button -> {
            title.setVisibility(View.GONE);
            llSearch.setVisibility(View.VISIBLE);
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchList.clear();
                for (Order order : orderList) {
                    if (order.getOriginOrder().getMarket().contains(s.toString().toUpperCase())) {
                        searchList.add(order);
                    }
                }
                isFiltering = true;
                setupListeners();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        marketManager = MarketOrderDataManager.getInstance(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recordAdapter = new MarketRecordAdapter(R.layout.adapter_item_p2p_record, null, this);
        recyclerView.setAdapter(recordAdapter);
        recordAdapter.addHeaderView(LayoutInflater.from(this)
                .inflate(R.layout.adapter_header_order, recyclerView, false));
        setupListeners();
        recordAdapter.setOnItemClickListener((adapter, view, position) -> {
            getOperation().addParameter("order", orderList.get(position));
            getOperation().forward(P2PRecordDetailActivity.class);
        });
        emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.market_order);
    }

    private void setupListeners() {
        if (isFiltering) {
            recordAdapter.setNewData(searchList);
            recordAdapter.setOnLoadMoreListener(() -> recordAdapter.loadMoreEnd(), recyclerView);
            recordAdapter.setOnItemClickListener((adapter, view, position) -> {
                getOperation().addParameter("order", searchList.get(position));
                getOperation().forward(P2PRecordDetailActivity.class);
            });
        } else {
            recordAdapter.setNewData(orderList);
            recordAdapter.setOnLoadMoreListener(() -> {
                if (recordAdapter.getData().size() >= totalCount) {
                    recordAdapter.loadMoreEnd();
                } else {
                    refreshOrders(currentPageIndex + 1);
                }
            }, recyclerView);
            recordAdapter.setOnItemClickListener((adapter, view, position) -> {
                getOperation().addParameter("order", orderList.get(position));
                getOperation().forward(P2PRecordDetailActivity.class);
            });
        }
    }

    @OnClick({R.id.left_btn1, R.id.cancel_text})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_text:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                hideSearch();
                break;
            case R.id.left_btn1:
                finish();
                break;
        }
    }

    public void refreshOrders(int page) {
        currentPageIndex = page == 0 ? currentPageIndex : page;
        marketManager.getLoopringService()
                .getOrders(WalletUtil.getCurrentAddress(this), OrderType.MARKET.getDescription(), currentPageIndex, pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PageWrapper<Order>>() {
                    @Override
                    public void onCompleted() {
                        refreshLayout.finishRefresh(true);
                        clLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LyqbLogger.log(e.getMessage());
                        recyclerView.setAdapter(emptyAdapter);
                        emptyAdapter.refresh();
                        refreshLayout.finishRefresh(true);
                        recordAdapter.loadMoreFail();
                        clLoading.setVisibility(View.GONE);
                        unsubscribe();
                    }

                    @Override
                    public void onNext(PageWrapper<Order> orderPageWrapper) {
                        hideSearch();
                        totalCount = orderPageWrapper.getTotal();
                        if (totalCount == 0) {
                            recyclerView.setAdapter(emptyAdapter);
                            emptyAdapter.refresh();
                        } else {
                            List<Order> list = new ArrayList<>();
                            for (Order order : orderPageWrapper.getData()) {
                                list.add(order.convert());
                            }
                            if (currentPageIndex == 1) {
                                recordAdapter.setNewData(list);
                            } else {
                                recordAdapter.addData(list);
                            }
                            orderList = recordAdapter.getData();
                            refreshLayout.finishRefresh(true);
                        }
                        clLoading.setVisibility(View.GONE);
                        recordAdapter.loadMoreComplete();
                        unsubscribe();
                    }
                });
    }

    private void hideSearch() {
        etSearch.setText("");
        isFiltering = false;
        setupListeners();
        title.setVisibility(View.VISIBLE);
        llSearch.setVisibility(View.GONE);
    }
}
