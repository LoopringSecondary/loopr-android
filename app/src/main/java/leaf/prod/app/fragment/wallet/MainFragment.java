package leaf.prod.app.fragment.wallet;

import java.math.BigDecimal;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.wallet.ReceiveActivity;
import leaf.prod.app.activity.setting.ManageWalletActivity;
import leaf.prod.app.activity.wallet.WalletDetailActivity;
import leaf.prod.app.activity.setupWallet.CoverActivity;
import leaf.prod.app.activity.setupWallet.ImportWalletActivity;
import leaf.prod.app.activity.trade.P2PConfirmActivity;
import leaf.prod.app.activity.wallet.ActivityScanerCode;
import leaf.prod.app.activity.wallet.AirdropActivity;
import leaf.prod.app.activity.wallet.AuthorityWebActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.activity.wallet.SendActivity;
import leaf.prod.app.activity.wallet.TokenListActivity;
import leaf.prod.app.adapter.wallet.MainWalletAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.presenter.wallet.MainFragmentPresenter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.QRCodeUitl;
import leaf.prod.walletsdk.model.QRCodeType;

public class MainFragment extends BaseFragment {

    public final static int BALANCE_SUCCESS = 1;

    private static int REQUEST_CODE = 1;  //二维码扫一扫code

    Unbinder unbinder;

    @BindView(R.id.title_text)
    TextView titleText;

    @BindView(R.id.right_btn)
    ImageView rightBtn;

    @BindView(R.id.wallet_count)
    TickerView walletCount;

    @BindView(R.id.wallet_address)
    TextView walletAddress;

    @BindView(R.id.wallet_qrcode)
    ImageView walletQrcode;

    @BindView(R.id.wallet_scan_tv)
    TextView walletScanTv;

    @BindView(R.id.wallet_receive_tv)
    TextView walletReceiveTv;

    @BindView(R.id.wallet_send_tv)
    TextView walletSendTv;

    @BindView(R.id.wallet_trades_tv)
    TextView walletTradesTv;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.ll_scan)
    LinearLayout llScan;

    @BindView(R.id.ll_receive)
    LinearLayout llReceive;

    @BindView(R.id.ll_send)
    LinearLayout llSend;

    @BindView(R.id.ll_trade)
    LinearLayout llTrade;

    @BindView(R.id.menu_scan)
    LinearLayout menuScan;

    @BindView(R.id.menu_add_assets)
    LinearLayout menuAddAssets;

    @BindView(R.id.menu_wallet)
    LinearLayout menuWallet;

    @BindView(R.id.ll_menu)
    LinearLayout llMenu;

    @BindView(R.id.refresh_layout)
    RefreshLayout refreshLayout;

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    @SuppressLint("HandlerLeak")
    Handler handlerBalance = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BALANCE_SUCCESS:
                default:
                    break;
            }
        }
    };

    private MainWalletAdapter mAdapter;

    private boolean showMenu = false;  //判断menu是否显示

    private BigDecimal moneyValue;  //钱包总金额

    private MainFragmentPresenter presenter;

    private boolean flag = true; //第一次进入

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, layout);
        walletCount.setAnimationInterpolator(new OvershootInterpolator());
        walletCount.setCharacterLists(TickerUtils.provideNumberList());
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showLoading(true);
        }
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            presenter.initObservable();
        });
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        this.presenter = new MainFragmentPresenter(this, this.getContext());
    }

    @Override
    protected void initView() {
        if (presenter.getWalletName() == null) {
            getOperation().forwardClearTop(CoverActivity.class);
        } else {
            titleText.setText(presenter.getWalletName());
            walletAddress.setText(presenter.getAddress());
        }
    }

    @Override
    protected void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MainWalletAdapter(R.layout.adapter_item_wallet, null);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (showMenu) {
                llMenu.setVisibility(View.GONE);
                showMenu = false;
            } else {
                getOperation().addParameter("symbol", presenter.getListAsset().get(position).getSymbol());
                getOperation().forward(WalletDetailActivity.class);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        flag = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flag) {
            flag = false;
            presenter.initObservable();
            presenter.initPushService();
        }
        if (showMenu) {
            showMenu = false;
            llMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }

    @OnClick({R.id.ll_scan, R.id.ll_receive, R.id.ll_send, R.id.ll_trade, R.id.menu_scan, R.id.menu_add_assets, R.id.menu_wallet, R.id.right_btn, R.id.ll_main, R.id.wallet_qrcode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_scan:  //scan 按钮
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                        startActivityForResult(new Intent(getContext(), ActivityScanerCode.class), REQUEST_CODE);
                    }
                }
                break;
            case R.id.ll_receive://receive 按钮
            case R.id.wallet_qrcode:
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    getOperation().forward(ReceiveActivity.class);
                }
                break;
            case R.id.ll_send://send转出 按钮
                if (showMenu) {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                } else {
                    getOperation().addParameter("send_address", "");
                    getOperation().forward(SendActivity.class);
                }
                break;
            case R.id.ll_trade://trade 按钮
                getOperation().forward(AirdropActivity.class);
                break;
            case R.id.right_btn:  //右上角添加按钮
                if (llMenu.getVisibility() == View.GONE) {
                    llMenu.setVisibility(View.VISIBLE);
                    showMenu = true;
                } else {
                    llMenu.setVisibility(View.GONE);
                    showMenu = false;
                }
                break;
            case R.id.ll_main://主布局页面，主要实现点击关闭menu页面
                llMenu.setVisibility(View.GONE);
                showMenu = false;
                break;
            case R.id.menu_scan:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    startActivityForResult(new Intent(getContext(), ActivityScanerCode.class), REQUEST_CODE);
                }
                break;
            case R.id.menu_add_assets:
                getOperation().forward(TokenListActivity.class);
                break;
            case R.id.menu_wallet:
                getOperation().forward(ManageWalletActivity.class);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString("result");
                switch (Objects.requireNonNull(QRCodeUitl.getQRCodeType(result))) {
                    case KEY_STORE:
                        getOperation().addParameter("result", result);
                        getOperation().forward(ImportWalletActivity.class);
                        break;
                    case TRANSFER:
                        getOperation().addParameter("send_address", result);
                        getOperation().forward(SendActivity.class);
                        break;
                    case P2P_ORDER:
                        getOperation().addParameter("p2p_order", result);
                        getOperation().forward(P2PConfirmActivity.class);
                        break;
                    case LOGIN:
                        getOperation().addParameter("qrcode_info", result);
                        getOperation().addParameter("qrcode_type", QRCodeType.LOGIN.name());
                        getOperation().forward(AuthorityWebActivity.class);
                        break;
                    case APPROVE:
                        getOperation().addParameter("qrcode_info", result);
                        getOperation().addParameter("qrcode_type", QRCodeType.APPROVE.name());
                        getOperation().forward(AuthorityWebActivity.class);
                        break;
                    case CONVERT:
                        getOperation().addParameter("qrcode_info", result);
                        getOperation().addParameter("qrcode_type", QRCodeType.CONVERT.name());
                        getOperation().forward(AuthorityWebActivity.class);
                        break;
                    case ORDER:
                        getOperation().addParameter("qrcode_info", result);
                        getOperation().addParameter("qrcode_type", QRCodeType.ORDER.name());
                        getOperation().forward(AuthorityWebActivity.class);
                        break;
                    case CANCEL_ORDER:
                        getOperation().addParameter("qrcode_info", result);
                        getOperation().addParameter("qrcode_type", QRCodeType.CANCEL_ORDER.name());
                        getOperation().forward(AuthorityWebActivity.class);
                        break;
                }
            }
        }
    }

    public MainWalletAdapter getmAdapter() {
        return mAdapter;
    }

    public void setWalletCount(String text) {
        if (walletCount != null)
            walletCount.setText(text);
    }

    public void finishRefresh() {
        if (refreshLayout != null)
            refreshLayout.finishRefresh(true);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showLoading(false);
        }
    }

    public void refresh() {
        presenter.initObservable();
    }
}
