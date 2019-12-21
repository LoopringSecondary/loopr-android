/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-10-15 1:10 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.trade;

import java.util.Objects;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.vondear.rxfeature.tool.RxQRCode;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.layout.WebLayout;
import leaf.prod.app.presenter.trade.H5DexPresenter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.H5ScanType;
import leaf.prod.walletsdk.util.DateUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.WalletUtil;

public class H5DexWebActivity extends BaseActivity {

    public String password;

    @BindView(R.id.ll_share_view)
    public ConstraintLayout clShareView;

    @BindView(R.id.qrcode_image)
    public ImageView qrCodeImage;

    @BindView(R.id.sell_info)
    public TextView sellInfo;

    @BindView(R.id.buy_info)
    public TextView buyInfo;

    @BindView(R.id.valid_until)
    public TextView validUntil;

    @BindView(R.id.price_A_buy)
    public TextView priceABuy;

    @BindView(R.id.price_A_sell)
    public TextView priceASell;

    @BindView(R.id.price_B_buy)
    public TextView priceBBuy;

    @BindView(R.id.price_B_sell)
    public TextView priceBSell;

    @BindView(R.id.title)
    TitleView title;

    /**
     * 输入密码dialog
     */
    private AlertDialog passwordDialog;

    private AgentWeb mAgentWeb;

    private H5DexPresenter presenter;

    private String url;

    private UMShareListener umShareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {
        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            RxToast.success(getResources().getString(R.string.share_success));
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (t.getMessage().contains("2008")) {//错误码
                RxToast.error(getResources().getString(R.string.share_failed_no_app));
            } else {
                RxToast.error(getResources().getString(R.string.share_failed, t.getMessage()));
            }
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
        }
    };

    /**
     * 初始化P层
     */
    @Override
    protected void initPresenter() {
        this.presenter = new H5DexPresenter(this, this);
    }

    /**
     * 初始化标题
     */
    @Override
    public void initTitle() {
        title.setBTitle(getString(R.string.h5_dex));
        title.clickLeftGoBack(getWContext());
    }

    /**
     * 初始化视图
     */
    @Override
    public void initView() {
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.ll_web_view);
        AgentWeb.PreAgentWeb preAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(linearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .defaultProgressBarColor()
                .setReceivedTitleCallback((view, title) -> {
                })
                .setWebChromeClient(new WebChromeClient())
                .setWebViewClient(new WebViewClient())
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setSecurityType(AgentWeb.SecurityType.strict)
                .setWebLayout(new WebLayout(this))
                .openParallelDownload() //打开并行下载 , 默认串行下载
                .setNotifyIcon(R.mipmap.download) //下载图标
                .setOpenOtherAppWays(DefaultWebClient.OpenOtherAppWays.DISALLOW) //打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownScheme() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready();
        url = getIntent().getStringExtra("url");
        mAgentWeb = preAgentWeb.go(url);
        mAgentWeb.getAgentWebSettings().getWebSettings().setMinimumFontSize(1);
        mAgentWeb.getAgentWebSettings().getWebSettings().setMinimumLogicalFontSize(1);
        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", presenter);
    }

    /**
     * 0
     * 初始化数据
     */
    @Override
    public void initData() {
        String p2pOrder = getIntent().getStringExtra("p2p_order");
        if (p2pOrder != null && !p2pOrder.isEmpty()) {
            presenter.type = H5ScanType.P2P_ORDER;
            presenter.scanContent = p2pOrder;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_h5_dex);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    public void call(String string) {
        Log.d("CALL_BACK", "Javascript string = " + string);
        mAgentWeb.getLoader().loadUrl(string);
    }

    public void showPasswordDialog() {
        if (passwordDialog == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_put_password, null);
            builder.setView(view);
            final EditText passwordInput = view.findViewById(R.id.password_input);
            view.findViewById(R.id.cancel).setOnClickListener(v -> passwordDialog.dismiss());
            view.findViewById(R.id.confirm).setOnClickListener(v -> {
                if (TextUtils.isEmpty(passwordInput.getText().toString())) {
                    RxToast.error(view.getContext().getResources().getString(R.string.put_password));
                } else {
                    try {
                        presenter.sign(WalletUtil.getCredential(this, passwordInput.getText().toString()));
                    } catch (Exception e) {
                        RxToast.error(getResources().getString(R.string.keystore_psw_error));
                    }
                }
            });
            builder.setCancelable(true);
            passwordDialog = null;
            passwordDialog = builder.create();
            passwordDialog.setCancelable(true);
            passwordDialog.setCanceledOnTouchOutside(true);
        } else {
            ((EditText) Objects.requireNonNull(passwordDialog.findViewById(R.id.password_input))).setText("");
        }
        passwordDialog.show();
    }

    public void hidePasswordDialog() {
        if (passwordDialog != null) {
            passwordDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    public void handleP2PShare(JSONObject p2pOrder) {
        runOnUiThread(() -> {
            try {
                JSONObject orderDetail = p2pOrder.getJSONObject("extra");
                RxQRCode.createQRCode(p2pOrder.getString("content"), qrCodeImage);
                sellInfo.setText(orderDetail.getString("amountS") + orderDetail.getString("tokenSell"));
                buyInfo.setText(orderDetail.getString("amountB") + orderDetail.getString("tokenBuy"));
                String date = DateUtil.formatDateTime(orderDetail.getLong("validUntil"), "MM-dd HH:mm");
                validUntil.setText(date);
                double amountB = orderDetail.getDouble("amountB");
                double amountS = orderDetail.getDouble("amountS");
                double priceBuy = amountB / amountS;
                double priceSell = amountS / amountB;
                priceABuy.setText("1 " + orderDetail.getString("tokenBuy"));
                priceASell.setText(NumberUtils.format1(priceSell, 4) + " " + orderDetail.getString("tokenSell"));
                priceBSell.setText("1 " + orderDetail.getString("tokenSell"));
                priceBBuy.setText(NumberUtils.format1(priceBuy, 4) + " " + orderDetail.getString("tokenBuy"));
                uShare();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void uShare() {
        UMImage umImage = new UMImage(getApplicationContext(), getBitmap(clShareView));
        umImage.setTitle(getResources().getString(R.string.wallet_address_share));
        umImage.setDescription(getResources().getString(R.string.wallet_address_share));
        ShareAction shareAction = new ShareAction(H5DexWebActivity.this);
        shareAction.setDisplayList(SHARE_MEDIA.QQ,
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.FACEBOOK)
                .setCallback(umShareListener).withMedia(umImage).open();
    }

    // 根据 layout 生成bitmap
    public Bitmap getBitmap(ConstraintLayout layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);
        return bmp;
    }
}
