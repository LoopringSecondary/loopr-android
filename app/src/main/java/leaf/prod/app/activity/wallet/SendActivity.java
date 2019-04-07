package leaf.prod.app.activity.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vondear.rxtool.view.RxToast;
import com.xw.repo.BubbleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.wallet.ContactSearchAdapter;
import leaf.prod.app.presenter.wallet.SendPresenter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.QRCodeType;
import leaf.prod.walletsdk.util.StringUtils;

public class SendActivity extends BaseActivity {

    public final static int SEND_SUCCESS = 3;

    public final static int SEND_FAILED = 4;

    public final static int ERROR_ONE = 5;

    public final static int ERROR_TWO = 6;

    public final static int ERROR_THREE = 7;

    public final static int ERROR_FOUR = 8;

    private static int REQUEST_CODE = 1;  //二维码扫一扫code

    private static int TOKEN_CODE = 2;  //选择币种code

    private static int CONTACT_CODE = 3;  //选择币种code

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.wallet_image)
    public ImageView walletImage;

    @BindView(R.id.wallet_symbol)
    public TextView walletSymbol;

    @BindView(R.id.send_wallet_name)
    public TextView sendWalletName;

    @BindView(R.id.send_wallet_count)
    public TextView sendWalletCount;

    @BindView(R.id.ll_manager_wallet)
    public LinearLayout llManagerWallet;

    @BindView(R.id.wallet_address)
    public MaterialEditText walletAddress;

    @BindView(R.id.address_toast)
    public TextView addressToast;

    @BindView(R.id.money_amount)
    public MaterialEditText moneyAmount;

    @BindView(R.id.wallet_name2)
    public TextView walletName2;

    @BindView(R.id.amount_toast)
    public TextView amountToast;

    @BindView(R.id.seekBar)
    public BubbleSeekBar seekBar;

    @BindView(R.id.transaction_fee)
    public TextView transactionFee;

    @BindView(R.id.btn_send)
    public Button btnSend;

    @BindView(R.id.ll_show_fee)
    public LinearLayout llShowFee;

    @BindView(R.id.rv_search_contacts)
    public RecyclerView rvSearchContacts;

    @BindView(R.id.ll_search_contacts)
    public LinearLayout llSearchContents;

    public Animation shakeAnimation;

    private SendPresenter presenter;

    @SuppressLint("HandlerLeak")
    public Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_SUCCESS:
                    hideProgress();
                    getOperation().addParameter("tokenAmount", "-" + moneyAmount.getText() + " " + presenter.getSendChoose());
                    getOperation().addParameter("address", walletAddress.getText().toString());
                    getOperation().forwardClearTop(SendSuccessActivity.class);
                    presenter.dismissPasswordDialog();
                    break;
                case ERROR_THREE:
                case ERROR_FOUR:
                case ERROR_ONE:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.keystore_psw_error));
                    break;
                case SEND_FAILED:
                case ERROR_TWO:
                    hideProgress();
                    getOperation().addParameter("error", getResources().getString(R.string.transfer_error));
                    getOperation().forwardClearTop(SendErrorActivity.class);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // FIXME: W/System.err: at leaf.prod.app.activity.SendActivity.onCreate(SendActivity.java:238)
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
        presenter = new SendPresenter(this, this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.send));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_scan, button -> {
            Intent intent = new Intent(this, ActivityScanerCode.class);
            intent.putExtra("restrict", QRCodeType.TRANSFER.name());
            startActivityForResult(intent, REQUEST_CODE);
        });
    }

    @Override
    public void initView() {
        presenter.initSeekbar();
        presenter.initMoneyAmount();
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_x);
        rvSearchContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvSearchContacts.setAdapter(new ContactSearchAdapter(R.layout.adapter_search_contact_list, null));
    }

    @Override
    public void initData() {
        presenter.updateBySymbol(null);
        presenter.updateTransactionFeeUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.initWalletAddress();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (llSearchContents.getVisibility() == View.VISIBLE) {
                mPoint.x = (int) ev.getRawX();
                mPoint.y = (int) ev.getRawY();
                if (!presenter.clickInView(mPoint, walletAddress) && !presenter.clickInView(mPoint, llSearchContents)) {
                    llSearchContents.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @OnClick({R.id.ll_manager_wallet, R.id.iv_contact, R.id.btn_send, R.id.ll_show_fee})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_manager_wallet:
                getOperation().forwardForResult(FlutterSendListChooseActivity.class, TOKEN_CODE);
                break;
            case R.id.iv_contact:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    startActivityForResult(new Intent(SendActivity.this, ContactListActivity.class), CONTACT_CODE);
                }
                break;
            case R.id.ll_show_fee:
                presenter.showFeeDialog();
                break;
            case R.id.btn_send:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    presenter.checkInfo();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.dismissPasswordDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //            处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString("result");
                LyqbLogger.log(result);
                walletAddress.setText(result);
            }
        } else if (requestCode == TOKEN_CODE) {
            if (resultCode == 1) {
                presenter.initSeekbar();
                presenter.updateBySymbol(data);
            }
        } else if (requestCode == CONTACT_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String address = bundle.getString("address");
                if (!StringUtils.isEmpty(address)) {
                    walletAddress.setText(address);
                }
            }
        }
    }
}
