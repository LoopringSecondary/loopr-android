package leaf.prod.app.fragment.trade;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.market.MarketRecordsActivity;
import leaf.prod.app.activity.market.MarketsActivity;
import leaf.prod.app.activity.trade.ConvertActivity;
import leaf.prod.app.activity.trade.P2PActivity;
import leaf.prod.app.fragment.BaseFragment;

/**
 *
 */
public class DappFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.ddex_layout)
    LinearLayout llDex;

    @BindView(R.id.p2p_layout)
    LinearLayout llP2P;

    @BindView(R.id.order_layout)
    LinearLayout llOrder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_dapp, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ddex_layout, R.id.p2p_layout, R.id.weth_wrap_layout, R.id.order_layout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ddex_layout:
                getOperation().forward(MarketsActivity.class);
                break;
            case R.id.p2p_layout:
                getOperation().forward(P2PActivity.class);
                break;
            case R.id.weth_wrap_layout:
                getOperation().forward(ConvertActivity.class);
                break;
            case R.id.order_layout:
                getOperation().forward(MarketRecordsActivity.class);
                break;
        }
    }
}
