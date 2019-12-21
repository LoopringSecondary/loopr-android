package leaf.prod.app.activity.market;

import java.util.Map;

import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.util.StringUtils;

public class MarketErrorActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.tv_error_info)
    TextView tvErrorInfo;

    @BindView(R.id.cl_need_token1)
    ConstraintLayout clNeedToken1;

    @BindView(R.id.cl_need_token2)
    ConstraintLayout clNeedToken2;

    @BindView(R.id.tv_token_amount1)
    TextView tvTokenAmount1;

    @BindView(R.id.tv_token_amount2)
    TextView tvTokenAmount2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_p2p_error);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitleNoBar(getResources().getString(R.string.send_result));
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        BalanceDataManager balanceDataManager = BalanceDataManager.getInstance(this);
        MarketOrderDataManager marketOrderDataManager = MarketOrderDataManager.getInstance(this);
        clNeedToken1.setVisibility(View.GONE);
        clNeedToken2.setVisibility(View.GONE);
        if (!StringUtils.isEmpty(getIntent().getStringExtra("error"))) {
            tvErrorInfo.setText(getIntent().getStringExtra("error"));
        }
        Map<String, Double> map = marketOrderDataManager.getBalanceInfo();
        if (map != null && map.size() > 0) {
            Double eth = map.get("MINUS_ETH"), lrc = map.get("MINUS_LRC");
            if (eth != null && eth != 0d) {
                tvTokenAmount1.setText(balanceDataManager.getFormattedBySymbol("ETH", eth) + " ETH");
                clNeedToken1.setVisibility(View.VISIBLE);
            }
            if (lrc != null && lrc != 0d) {
                tvTokenAmount2.setText(balanceDataManager.getFormattedBySymbol("LRC", lrc) + " LRC");
                clNeedToken2.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick({R.id.btn_return})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                getOperation().forward(MainActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        getOperation().forward(MainActivity.class);
    }
}
