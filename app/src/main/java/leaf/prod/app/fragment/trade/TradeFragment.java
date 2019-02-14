package leaf.prod.app.fragment.trade;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.adapter.ViewPageAdapter;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.fragment.market.MarketsFragment;
import leaf.prod.app.presenter.market.TradeFragmentPresenter;
import leaf.prod.walletsdk.manager.MarketPriceDataManager;
import leaf.prod.walletsdk.model.MarketsType;
import leaf.prod.walletsdk.model.Ticker;

/**
 *
 */
public class TradeFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.market_tab)
    TabLayout marketTab;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    private List<Ticker> list;

    private List<Ticker> listSearch = new ArrayList<>();

    private TradeFragmentPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_trade, container, false);
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
        List<Fragment> fragments = new ArrayList<>();
        String[] titles = new String[MarketsType.values().length];
        for (MarketsType type : MarketsType.values()) {
            MarketsFragment fragment = new MarketsFragment();
            fragment.setMarketsType(type);
            fragments.add(type.ordinal(), fragment);
            titles[type.ordinal()] = type.name();
        }
        titles[0] = getString(R.string.Favorites);
        list = MarketPriceDataManager.getInstance(getContext()).getAllTickers();
        presenter = new TradeFragmentPresenter(this, getContext());
        presenter.setFragments(fragments);
        presenter.refreshTickers();
        presenter.updateAdapter(false, list);
        setupViewPager(fragments, titles);
    }

    private void setupViewPager(List<Fragment> fragments, String[] titles) {
        marketTab.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(titles.length - 1);
        viewPager.setAdapter(new ViewPageAdapter(getChildFragmentManager(), fragments, titles));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                MarketsFragment fragment = (MarketsFragment) fragments.get(position);
                fragment.updateAdapter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
