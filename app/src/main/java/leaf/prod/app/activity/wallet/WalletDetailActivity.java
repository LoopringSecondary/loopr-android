package leaf.prod.app.activity.wallet;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.DefaultWebViewActivity;
import leaf.prod.app.activity.trade.ConvertActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.setupwallet.WalletAllAdapter;
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

public class WalletDetailActivity extends BaseActivity {

    private static final int PAGE_SIZE = 20;

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

    private String symbol;

    private WalletAllAdapter mAdapter;

    private NoDataAdapter emptyAdapter;

    private LoopringService loopringService = new LoopringService();

    private GasDataManager gasManager;

    private TokenDataManager tokenManager;

    private MarketcapDataManager priceManager;

    private BalanceDataManager balanceManager;

    private String address;

    private List<Transaction> list;

    private int txTotalCount;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        balanceManager = BalanceDataManager.getInstance(this);
        setContentView(R.layout.activity_wallet_detail);
        ButterKnife.bind(this);
        walletMoney.setAnimationInterpolator(new OvershootInterpolator());
        walletMoney.setCharacterLists(TickerUtils.provideNumberList());
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        refreshLayout.setOnRefreshListener(refreshLayout -> getTxsFromRelay(currentPageIndex = 1));
    }

    @Override
    protected void initPresenter() {
        gasManager = GasDataManager.getInstance(this);
        tokenManager = TokenDataManager.getInstance(this);
        priceManager = MarketcapDataManager.getInstance(this);
    }

    @Override
    public void initTitle() {
        symbol = getIntent().getStringExtra("symbol");
        title.setBTitle(symbol);
        title.clickLeftGoBack(getWContext());
        if (symbol.equalsIgnoreCase("ETH") || symbol.equalsIgnoreCase("WETH")) {
            title.setRightText(getString(R.string.convert), button -> getOperation().forward(ConvertActivity.class));
        }
        walletMoney.setText(balanceManager.getAssetBySymbol(symbol).getValueShown());
        walletDollar.setText(balanceManager.getAssetBySymbol(symbol).getLegalShown());
        list = new ArrayList<>();
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        address = WalletUtil.getCurrentAddress(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);  //助记词提示列表
        mAdapter = new WalletAllAdapter(R.layout.adapter_item_wallet_all, null, symbol);
        mAdapter.setOnItemClickListener((adapter, view, position) -> showDetailDialog(mAdapter.getItem(position)));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(() -> {
            if (mAdapter.getData().size() >= txTotalCount) {
                //数据全部加载完毕
                mAdapter.loadMoreEnd();
            } else {
                //成功获取更多数据
                getTxsFromRelay(currentPageIndex++);
            }
        }, recyclerView);
        emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.transation);
        getTxsFromRelay(currentPageIndex = 1);
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
                        refreshLayout.finishRefresh(true);
                        mAdapter.loadMoreFail();
                        unsubscribe();
                    }

                    @Override
                    public void onNext(TransactionPageWrapper transactionPageWrapper) {
                        txTotalCount = transactionPageWrapper.getTotal();
                        if (page == 1) {
                            mAdapter.setNewData(transactionPageWrapper.getData());
                        } else {
                            mAdapter.addData(transactionPageWrapper.getData());
                        }
                        walletMoney.setText(balanceManager.getAssetBySymbol(symbol).getValueShown());
                        walletDollar.setText(balanceManager.getAssetBySymbol(symbol).getLegalShown());
                        refreshLayout.finishRefresh(true);
                        mAdapter.loadMoreComplete();
                        unsubscribe();
                    }
                });
    }

    public void showDetailDialog(Transaction tx) {
        if (dialog == null) {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme);//
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_trade_detail, null);
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
            String currency = CurrencyUtil.format(this, value * price);
            switch (tx.getType()) {
                case SEND:
                case SELL:
                case CONVERT_OUTCOME:
                    valueShown = "-" + valueShown + " " + tx.getSymbol();
                    txAmount.setTextColor(this.getResources().getColor(R.color.colorRed));
                    break;
                case BUY:
                case RECEIVE:
                case CONVERT_INCOME:
                    valueShown = "+" + valueShown + " " + tx.getSymbol();
                    txAmount.setTextColor(this.getResources().getColor(R.color.colorGreen));
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
                txStatus.setTextColor(this.getResources().getColor(R.color.colorGreen));
                break;
            case PENDING:
                txStatus.setTextColor(this.getResources().getColor(R.color.colorPending));
                break;
            case FAILED:
                txStatus.setTextColor(this.getResources().getColor(R.color.colorRed));
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
        String currency = CurrencyUtil.format(this, gasValue * price);
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
                getOperation().forward(ReceiveActivity.class);
                break;
            case R.id.btn_send:
                getOperation().addParameter("symbol", symbol);
                getOperation().forward(SendActivity.class);
                break;
        }
    }
}
