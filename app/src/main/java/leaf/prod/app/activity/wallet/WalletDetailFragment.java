package leaf.prod.app.activity.wallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.DefaultWebViewActivity;
import leaf.prod.app.activity.trade.ConvertActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.setupwallet.WalletAllAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.fragment.wallet.MainFragment;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.GasDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.TxType;
import leaf.prod.walletsdk.model.response.relay.Transaction;
import leaf.prod.walletsdk.model.response.relay.TransactionPageWrapper;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WalletDetailFragment extends BaseFragment {

    private static final int PAGE_SIZE = 20;

    Unbinder unbinder;

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.wallet_money)
    TickerView walletMoney;

    @BindView(R.id.wallet_dollar)
    TextView walletDollar;

    @BindView(R.id.wallet_qrcode)
    ImageView walletQrcode;

    @BindView(R.id.btn_receive)
    Button btnReceive;

    @BindView(R.id.btn_send)
    Button btnSend;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;

    @BindView(R.id.switch_news)
    SmartRefreshLayout switchNews;

    @BindView(R.id.cl_loading)
    ConstraintLayout clLoading;

    private String symbol;

    private WalletAllAdapter mAdapter;

    private NoDataAdapter emptyAdapter;

    private LoopringService loopringService = new LoopringService();

    private GasDataManager gasManager;

    private TokenDataManager tokenManager;

    private MarketcapDataManager priceManager;

    private BalanceDataManager balanceManager;

    private String address;

    private int currentPageIndex = 1;

    /**
     * @param context
     */
    private AlertDialog dialog;

    private TextView txAmount;

    private TextView txStatus;

    private TextView txAddress;

    private TextView txID;

    private TextView txGas;

    private TextView txDate;

    private TextView txApprove;

    private RelativeLayout txApproveLayout;

    private RelativeLayout txReceivedLayout;

    private int[] locations = new int[2];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_wallet_detail, container, false);
        unbinder = ButterKnife.bind(this, layout);
        symbol = getArguments().getString("symbol");
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        balanceManager = BalanceDataManager.getInstance(getContext());
        gasManager = GasDataManager.getInstance(getContext());
        tokenManager = TokenDataManager.getInstance(getContext());
        priceManager = MarketcapDataManager.getInstance(getContext());
        walletMoney.setAnimationInterpolator(new OvershootInterpolator());
        walletMoney.setCharacterLists(TickerUtils.provideNumberList());
    }

    @Override
    public void initView() {
        clLoading.setVisibility(View.VISIBLE);
        title.setBTitle(symbol);
        if (symbol.equalsIgnoreCase("ETH") || symbol.equalsIgnoreCase("WETH")) {
            title.setRightText(getString(R.string.convert), button -> getOperation().forward(ConvertActivity.class));
        }
        walletMoney.setText(balanceManager.getAssetBySymbol(symbol).getValueShown());
        walletDollar.setText(balanceManager.getAssetBySymbol(symbol).getLegalShown());
        title.clickLeftGoBack(((WalletDetailActivity) getActivity()).getWContext());
    }

    @Override
    public void initData() {
        address = WalletUtil.getCurrentAddress(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);  //助记词提示列表
        mAdapter = new WalletAllAdapter(R.layout.adapter_item_wallet_all, null, symbol);
        mAdapter.setOnItemClickListener((adapter, view, position) -> showDetailDialog(mAdapter.getItem(position)));
        recyclerView.setAdapter(mAdapter);
        refreshLayout.setOnRefreshListener(refreshLayout -> getTxsFromRelay(currentPageIndex = 1));
        refreshLayout.setOnLoadMoreListener(refreshLayout -> getTxsFromRelay(++currentPageIndex));
        switchNews.setOnRefreshListener(refreshLayout -> {
            EventBus.getDefault().post(new MainFragment.Event(0));
            refreshLayout.finishRefresh(true);
        });
        emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.transation);
        getTxsFromRelay(currentPageIndex = 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((WalletDetailActivity) this.getActivity()).registerMyTouchListener(myTouchListener);
    }

    private void getTxsFromRelay(int page) {
        loopringService.getTransactions(address, symbol, page, PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TransactionPageWrapper>() {
                    @Override
                    public void onCompleted() {
                        refreshLayout.finishRefresh(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        recyclerView.setAdapter(emptyAdapter);
                        emptyAdapter.refresh();
                        refreshLayout.finishRefresh(false);
                        refreshLayout.finishLoadMore(false);
                        unsubscribe();
                        clLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(TransactionPageWrapper transactionPageWrapper) {
                        if (page == 1) {
                            mAdapter.setNewData(transactionPageWrapper.getData());
                        } else {
                            mAdapter.addData(transactionPageWrapper.getData());
                        }
                        walletMoney.setText(balanceManager.getAssetBySymbol(symbol).getValueShown());
                        walletDollar.setText(balanceManager.getAssetBySymbol(symbol).getLegalShown());
                        if (page == 1) {
                            refreshLayout.finishRefresh(true);
                        } else if (mAdapter.getData().size() < transactionPageWrapper.getTotal()) {
                            refreshLayout.finishLoadMore(true);
                        } else {
                            refreshLayout.finishLoadMoreWithNoMoreData();
                        }
                        unsubscribe();
                        clLoading.setVisibility(View.GONE);
                    }
                });
    }

    public void showDetailDialog(Transaction tx) {
        if (dialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext(), R.style.DialogTheme);//
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_trade_detail, null);
            builder.setView(view);
            builder.setCancelable(true);
            txApprove = view.findViewById(R.id.tx_approve);
            txApproveLayout = view.findViewById(R.id.tx_approve_layout);
            txReceivedLayout = view.findViewById(R.id.tx_received_layout);
            txAmount = view.findViewById(R.id.tx_detail_amount);
            txStatus = view.findViewById(R.id.tx_detail_status);
            txAddress = view.findViewById(R.id.tx_detail_address);
            txID = view.findViewById(R.id.tx_detail_ID);
            txGas = view.findViewById(R.id.tx_detail_gas);
            txDate = view.findViewById(R.id.receive_date);
            builder.setCancelable(true);
            dialog = builder.create();
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
        }
        if (tx.getType() == TxType.APPROVE) {
            txApprove.setText(getResources().getString(R.string.approve_details, symbol));
            txApproveLayout.setVisibility(View.VISIBLE);
            txReceivedLayout.setVisibility(View.GONE);
        } else if (tx.getType() == TxType.OTHER) {
            txApprove.setText(getResources().getString(R.string.other));
            txApproveLayout.setVisibility(View.VISIBLE);
            txReceivedLayout.setVisibility(View.GONE);
        } else {
            txApproveLayout.setVisibility(View.GONE);
            txReceivedLayout.setVisibility(View.VISIBLE);
        }
        setAmount(tx);
        setStatus(tx);
        setAddress(tx);
        setID(tx);
        setGas(tx);
        setDate(tx);
        dialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void setAmount(Transaction tx) {
        String result = "--";
        Double price = priceManager.getPriceBySymbol(tx.getSymbol());
        Double value = tokenManager.getDoubleFromWei(tx.getSymbol(), tx.getValue());
        if (price != null && value != null) {
            int precision = balanceManager.getPrecisionBySymbol(tx.getSymbol());
            String valueShown = NumberUtils.format1(value, precision);
            String currency = CurrencyUtil.format(getContext(), value * price);
            switch (tx.getType()) {
                case SEND:
                case SELL:
                case CONVERT_OUTCOME:
                    valueShown = "-" + valueShown + " " + tx.getSymbol();
                    txAmount.setTextColor(getContext().getResources().getColor(R.color.colorRed));
                    break;
                case BUY:
                case RECEIVE:
                case CONVERT_INCOME:
                    valueShown = "+" + valueShown + " " + tx.getSymbol();
                    txAmount.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
                    break;
                default:
            }
            result = valueShown + " ≈ " + currency;
        }
        txAmount.setText(result);
    }

    private void setStatus(Transaction tx) {
        switch (tx.getStatus()) {
            case SUCCESS:
                txStatus.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
                break;
            case PENDING:
                txStatus.setTextColor(getContext().getResources().getColor(R.color.colorPending));
                break;
            case FAILED:
                txStatus.setTextColor(getContext().getResources().getColor(R.color.colorRed));
                break;
        }
        txStatus.setText(tx.getStatus().getDescription());
    }

    private void setAddress(Transaction tx) {
        String etherUrl = "https://etherscan.io/address/";
        if (tx.getType() == TxType.RECEIVE) {
            etherUrl += tx.getFrom();
            txAddress.setText(tx.getFrom());
        } else {
            etherUrl += tx.getTo();
            txAddress.setText(tx.getTo());
        }
        final String url = etherUrl;
        txAddress.setOnClickListener(v -> {
            getOperation().addParameter("url", url);
            getOperation().addParameter("title", getString(R.string.etherscan));
            getOperation().forward(DefaultWebViewActivity.class);
        });
    }

    private void setID(Transaction tx) {
        String etherUrl = "https://etherscan.io/tx/";
        txID.setText(tx.getTxHash());
        etherUrl += tx.getTxHash();
        final String url = etherUrl;
        txID.setOnClickListener(v -> {
            getOperation().addParameter("url", url);
            getOperation().addParameter("title", getString(R.string.etherscan));
            getOperation().forward(DefaultWebViewActivity.class);
        });
    }

    private void setGas(Transaction tx) {
        String gasAmount = gasManager.getGasAmountInETH(tx.getGas_used(), tx.getGas_price());
        double gasValue = Double.parseDouble(gasAmount);
        Double price = priceManager.getPriceBySymbol("ETH");
        String currency = CurrencyUtil.format(getContext(), gasValue * price);
        txGas.setText(gasAmount + " ETH ≈ " + currency);
    }

    private void setDate(Transaction tx) {
        String date = DateUtil.timeStampToDateTime3(tx.getCreateTime());
        txDate.setText(date);
    }

    @OnClick({R.id.btn_receive, R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_receive:
                getOperation().forward(FlutterReceiveActivity.class);
                break;
            case R.id.btn_send:
                getOperation().addParameter("symbol", symbol);
                getOperation().forward(SendActivity.class);
                break;
        }
    }

    /**
     * 触摸判定
     */
    private MainActivity.MyTouchListener myTouchListener = ev -> {
        refreshLayout.getLocationInWindow(locations);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getY() > locations[1]) {
                switchNews.setEnableRefresh(false);
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            switchNews.setEnableRefresh(true);
        }
    };
}
