package leaf.prod.app.fragment.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.constraintlayout.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.infomation.NewsInfoActivity;
import leaf.prod.app.fragment.BaseFragment;
import leaf.prod.app.layout.MyTailRecyclerView;
import leaf.prod.app.presenter.infomation.NewsPresenter;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.walletsdk.model.response.crawler.News;

/**
 *
 */
public class NewsFragment extends BaseFragment {

    Unbinder unbinder;

    @BindView(R.id.cl_loading)
    public ConstraintLayout clLoading;

    @BindView(R.id.recycler_view)
    public MyTailRecyclerView recyclerView;

    private NewsPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_news, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
        presenter = new NewsPresenter(this, getContext());
    }

    @Override
    protected void initView() {
        clLoading.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {
        presenter.initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnItemClick(News news) {
        LyqbLogger.log(news.getTitle());
        Intent intent = new Intent(getContext(), NewsInfoActivity.class);
        intent.putExtra("data", news);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        recyclerView.post(() -> recyclerView.scrollToPosition(1));
    }
}
