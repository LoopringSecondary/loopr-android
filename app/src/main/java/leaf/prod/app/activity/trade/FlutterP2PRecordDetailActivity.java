/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:23 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.trade;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.flutter.facade.Flutter;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.presenter.trade.P2PRecordDetailPresenter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.Order;
import leaf.prod.walletsdk.model.OrderStatus;
import leaf.prod.walletsdk.model.OrderType;
import leaf.prod.walletsdk.model.OriginOrder;
import leaf.prod.walletsdk.model.P2PSide;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;

public class FlutterP2PRecordDetailActivity extends BaseActivity {

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.iv_token_s)
    public ImageView ivTokenS;

    @BindView(R.id.tv_token_s)
    public TextView tvTokenS;

    @BindView(R.id.iv_token_b)
    public ImageView ivTokenB;

    @BindView(R.id.tv_token_b)
    public TextView tvTokenB;

    @BindView(R.id.tv_sell_token)
    public TextView tvTokenSell;

    @BindView(R.id.tv_buy_token)
    public TextView tvTokenBuy;

    @BindView(R.id.tv_sell_amount)
    public TextView tvAmountS;

    @BindView(R.id.tv_buy_amount)
    public TextView tvAmountB;

    @BindView(R.id.tv_sell_price)
    public TextView tvPriceS;

    @BindView(R.id.tv_buy_price)
    public TextView tvPriceB;

    @BindView(R.id.ll_share_view)
    public ConstraintLayout shareView;

    @BindView(R.id.qrcode_image)
    public ImageView qrCodeImage;

    @BindView(R.id.sell_info)
    public TextView sellInfo;

    @BindView(R.id.buy_info)
    public TextView buyInfo;

    @BindView(R.id.valid_until)
    public TextView tvValidUntil;

    @BindView(R.id.price_A_buy)
    public TextView priceABuy;

    @BindView(R.id.price_A_sell)
    public TextView priceASell;

    @BindView(R.id.price_B_buy)
    public TextView priceBBuy;

    @BindView(R.id.price_B_sell)
    public TextView priceBSell;

    P2PRecordDetailPresenter presenter;

    private Order order;

    private TokenDataManager tokenDataManager;

    private BalanceDataManager balanceDataManager;

    private P2POrderDataManager p2pOrderManager;

    private MarketcapDataManager marketDataManager;

    private String priceStr = "";

    private String ratioStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Flutter
        setContentView(R.layout.activity_p2p_record_detail);
        ButterKnife.bind(this);
        tokenDataManager = TokenDataManager.getInstance(this);
        balanceDataManager = BalanceDataManager.getInstance(this);
        p2pOrderManager = P2POrderDataManager.getInstance(this);
        marketDataManager = MarketcapDataManager.getInstance(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
        presenter = new P2PRecordDetailPresenter(this, this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.order_detail));
        title.clickLeftGoBack(getWContext());
        order = (Order) getIntent().getSerializableExtra("order");
        if (order.getOriginOrder().getOrderType() == OrderType.P2P && order.getOriginOrder()
                .getP2pSide() == P2PSide.MAKER
                && (order.getOrderStatus() == OrderStatus.OPENED || order.getOrderStatus() == OrderStatus.WAITED)) {
            title.setRightImageButton(R.mipmap.icon_title_qrcode, button -> {
                if (!(ButtonClickUtil.isFastDoubleClick(1))) {
                    String authAddr = order.getOriginOrder().getAuthAddr().toLowerCase();
                    String p2pContent = (String) SPUtils.get(getApplicationContext(), authAddr, "");
                    if (!p2pContent.isEmpty() && p2pContent.contains("-")) {
                        String qrCode = p2pOrderManager.generateQRCode(order.getOriginOrder());
                        presenter.showShareDialog(qrCode);
                    } else {
                        RxToast.error(getString(R.string.detail_qr_error));
                    }
                }
            });
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        // TODO: update once flutterView is ready
        if (order != null) {
            setOrderStatus();
            OriginOrder originOrder = order.getOriginOrder();
            if (originOrder != null) {
                setOverview(originOrder);
            }
        }

        FlutterView flutterView = Flutter.createView(
                this,
                getLifecycle(),
                "orderDetail"
        );

        new MethodChannel(flutterView, "orderDetail").setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        if (call.method.equals("orderDetail.get")) {

                            String lrcFee = balanceDataManager.getFormattedBySymbol("LRC", order.getOriginOrder().getLrc());
                            String lrcCurrency = marketDataManager.getCurrencyBySymbol("LRC", order.getOriginOrder().getLrc());
                            String tvTradingFee = lrcFee + " LRC ≈ " + lrcCurrency;

                            String validSince = DateUtil.formatDateTime(order.getOriginOrder().getValidS() * 1000L, "MM-dd HH:mm");
                            String validUntil = DateUtil.formatDateTime(order.getOriginOrder().getValidU() * 1000L, "MM-dd HH:mm");
                            String tvLiveTime = validSince + " ~ " + validUntil;

                            List<Object> greetings = new ArrayList<Object>();
                            greetings.add(setOrderStatus());
                            greetings.add(priceStr);
                            greetings.add(tvTradingFee);
                            greetings.add(ratioStr);
                            greetings.add(order.getOriginOrder().getHash());
                            greetings.add(tvLiveTime);

                            // This is to send data to Flutter
                            result.success(greetings);
                        }
                    }
                }
        );

        // Define layout for Flutter
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels-260);
        layout.topMargin = 260;
        this.addContentView(flutterView, layout);

    }

    private void setOverview(OriginOrder order) {
        int resourceB = tokenDataManager.getTokenBySymbol(order.getTokenB()).getImageResId();
        int resourceS = tokenDataManager.getTokenBySymbol(order.getTokenS()).getImageResId();
        String amountB = balanceDataManager.getFormattedBySymbol(order.getTokenB(), order.getAmountBuy());
        String amountS = balanceDataManager.getFormattedBySymbol(order.getTokenS(), order.getAmountSell());
        String currencyB = marketDataManager.getCurrencyBySymbol(order.getTokenB(), order.getAmountBuy());
        String currencyS = marketDataManager.getCurrencyBySymbol(order.getTokenS(), order.getAmountSell());
        Double price = order.getAmountSell() / order.getAmountBuy();
        priceStr = NumberUtils.format1(price, 6) + " " + order.getTokenS() + " / " + order.getTokenB();

        Double ratio = this.order.getDealtAmountSell() / order.getAmountSell();
        // Use NumberFormat
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        ratioStr = formatter.format(ratio);

        if (resourceB == 0) {
            ivTokenB.setVisibility(View.INVISIBLE);
            tvTokenB.setVisibility(View.VISIBLE);
            tvTokenB.setText(order.getTokenB());
        } else {
            ivTokenB.setVisibility(View.VISIBLE);
            tvTokenB.setVisibility(View.INVISIBLE);
            ivTokenB.setImageResource(resourceB);
        }
        if (resourceS == 0) {
            ivTokenS.setVisibility(View.INVISIBLE);
            tvTokenS.setVisibility(View.VISIBLE);
            tvTokenS.setText(order.getTokenS());
        } else {
            ivTokenS.setVisibility(View.VISIBLE);
            tvTokenS.setVisibility(View.INVISIBLE);
            ivTokenS.setImageResource(resourceS);
        }

        tvTokenBuy.setText(getString(R.string.buy) + " " + order.getTokenB());
        tvTokenSell.setText(getString(R.string.sell) + " " + order.getTokenS());
        tvAmountB.setText(amountB);
        tvAmountS.setText(amountS);
        tvPriceB.setText(currencyB);
        tvPriceS.setText(currencyS);

        sellInfo.setText(amountS + " " + order.getTokenS());
        buyInfo.setText(amountB + " " + order.getTokenB());

        double priceBuy = order.getAmountBuy() / order.getAmountSell();
        double priceSell = order.getAmountSell() / order.getAmountBuy();
        priceABuy.setText("1 " + order.getTokenB());
        priceASell.setText(NumberUtils.format1(priceSell, 4) + " " + order.getTokenS());
        priceBSell.setText("1 " + order.getTokenS());
        priceBBuy.setText(NumberUtils.format1(priceBuy, 4) + " " + order.getTokenB());
    }

    private String setOrderStatus(){
        String status = "";
        switch (order.getOrderStatus()) {
            case OPENED:
                status = OrderStatus.OPENED.getDescription(this);
                break;
            case WAITED:
                status = OrderStatus.WAITED.getDescription(this);
                break;
            case FINISHED:
                status = OrderStatus.FINISHED.getDescription(this);
                break;
            case CUTOFF:
                status = OrderStatus.CUTOFF.getDescription(this);
                break;
            case CANCELLED:
                status = OrderStatus.CANCELLED.getDescription(this);
                break;
            case EXPIRED:
                status = OrderStatus.EXPIRED.getDescription(this);
                break;
            case LOCKED:
                status = OrderStatus.LOCKED.getDescription(this);
                break;
        }
        return status;
    }
}