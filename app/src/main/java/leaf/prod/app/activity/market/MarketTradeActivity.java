/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:23 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.activity.market;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import androidx.core.app.Fragment;
import androidx.core.view.ViewPager;
import android.view.WindowManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.fragment.market.MarketTradeFragment;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.MarketOrderDataManager;
import leaf.prod.walletsdk.model.TradeType;

public class MarketTradeActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.trade_tab)
    TabLayout tradeTab;

    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    private MarketOrderDataManager orderDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_market_trade);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
        orderDataManager = MarketOrderDataManager.getInstance(this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(orderDataManager.getTradePair());
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_order_history, button -> getOperation().forward(MarketRecordsActivity.class));
    }

    @Override
    public void initView() {
        List<Fragment> fragments = new ArrayList<>();
        String[] titles = new String[TradeType.values().length];
        for (TradeType type : TradeType.values()) {
            MarketTradeFragment fragment = new MarketTradeFragment();
            fragment.setTradeType(type);
            fragments.add(type.ordinal(), fragment);
        }
        titles[0] = getString(R.string.buy);
        titles[1] = getString(R.string.sell);
        setupViewPager(fragments, titles);
    }

    private void setupViewPager(List<Fragment> fragments, String[] titles) {
        tradeTab.setupWithViewPager(viewPager);
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), fragments, titles));
        viewPager.setCurrentItem(orderDataManager.getType().ordinal());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                orderDataManager.setType(TradeType.getByIndex(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void initData() {
        orderDataManager.setPriceFromDepth(getIntent().getStringExtra("priceFromDepth"));
    }
}
