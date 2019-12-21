/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-12-18 10:16 AM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.wallet;

import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.presenter.wallet.AirdropPresenter;
import leaf.prod.app.views.TitleView;

public class AirdropActivity extends BaseActivity {

    private static int REQUEST_CODE = 1;

    @BindView(R.id.airdrop_addr)
    public MaterialEditText airdropAddress;

    @BindView(R.id.airdrop_amount)
    public MaterialEditText airdropAmount;

    @BindView(R.id.btn_claim)
    public Button claimButton;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    @BindView(R.id.ae_loading)
    public LottieAnimationView aeLoading;

    @BindView(R.id.address_tip)
    public TextView addressTip;

    @BindView(R.id.amount_tip)
    public TextView amountTip;

    @BindView(R.id.ll_airdrop_date)
    public LinearLayout llAirdropDate;

    @BindView(R.id.date_tip)
    public TextView dateTip;

    @BindView(R.id.airdrop_date)
    public MaterialEditText airdropDate;

    @BindView(R.id.title)
    TitleView title;

    /**
     * 初始化P层
     */
    @Override
    protected void initPresenter() {
        new AirdropPresenter(this, this);
    }

    /**
     * 初始化标题
     */
    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.airdrop_title));
        title.clickLeftGoBack(getWContext());
        //        title.setRightImageButton(R.mipmap.icon_scan, button -> {
        //            Intent intent = new Intent(this, ActivityScanerCode.class);
        //            intent.putExtra("restrict", QRCodeType.TRANSFER.name());
        //            startActivityForResult(intent, REQUEST_CODE);
        //        });
    }

    /**
     * 初始化视图
     */
    @Override
    public void initView() {
    }

    /**
     * 0
     * 初始化数据
     */
    @Override
    public void initData() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_airdrop);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }
}
