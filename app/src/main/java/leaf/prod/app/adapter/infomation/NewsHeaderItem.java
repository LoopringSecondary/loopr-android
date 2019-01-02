package leaf.prod.app.adapter.infomation;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ramotion.garlandview.header.HeaderDecorator;
import com.ramotion.garlandview.header.HeaderItem;
import com.ramotion.garlandview.inner.InnerLayoutManager;
import com.ramotion.garlandview.inner.InnerRecyclerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.walletsdk.model.NewsHeader;
import leaf.prod.walletsdk.model.response.crawler.News;
import leaf.prod.walletsdk.model.response.crawler.NewsPageWrapper;
import leaf.prod.walletsdk.service.CrawlerService;
import leaf.prod.walletsdk.util.DpUtil;
import leaf.prod.walletsdk.util.LanguageUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-22 4:25 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class NewsHeaderItem extends HeaderItem {

    private final static float ANSWER_RATIO_START = 0.75f;

    private final static float ANSWER_RATIO_MAX = 0.35f;

    private final static float ANSWER_RATIO_DIFF = ANSWER_RATIO_START - ANSWER_RATIO_MAX;

    private final static float MIDDLE_RATIO_START = 0.7f;

    private final static float MIDDLE_RATIO_MAX = 0.1f;

    private final static float MIDDLE_RATIO_DIFF = MIDDLE_RATIO_START - MIDDLE_RATIO_MAX;

    private static int headerHeight;

    @BindView(R.id.irv_header)
    public InnerRecyclerView headerRecyclerView;

    @BindView(R.id.tv_header1)
    public TextView tvHeader1;

    @BindView(R.id.tv_header2)
    public TextView tvHeader2;

    @BindView(R.id.cl_header)
    public ConstraintLayout clHeader;

    @BindView(R.id.header_alpha)
    public View headAlpha;

    @BindView(R.id.refresh_layout)
    public SmartRefreshLayout refreshLayout;

    private InnerLayoutManager layoutManager;

    private CrawlerService crawlerService;

    private NewsHeader.NewsType newsType;

    private int index = 0;

    private boolean mIsScrolling;

    private View view;

    public NewsHeaderItem(View itemView, RecyclerView.RecycledViewPool pool) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.view = itemView;
        headerHeight = DpUtil.dp2Int(view.getContext(), 170);
        // Init header
        headerRecyclerView.setAdapter(new NewsBodyAdapter(headerRecyclerView.getContext()));
        headerRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mIsScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                onItemScrolled(recyclerView);
            }
        });
        headerRecyclerView.addItemDecoration(new HeaderDecorator(headerHeight, DpUtil.dp2Int(view.getContext(), 12)));
        headerRecyclerView.setRecycledViewPool(pool);
        crawlerService = new CrawlerService();
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            if (newsType == NewsHeader.NewsType.NEWS_FLASH) {
                setFlash(index = 0);
            } else {
                setInformation(index = 0);
            }
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            if (newsType == NewsHeader.NewsType.NEWS_FLASH) {
                setFlash(++index);
            } else {
                setInformation(++index);
            }
        });
    }

    @Override
    public boolean isScrolling() {
        return mIsScrolling;
    }

    @Override
    public InnerRecyclerView getViewGroup() {
        return headerRecyclerView;
    }

    void setContent(@NonNull NewsHeader newsHeader) {
        this.newsType = newsHeader.getNewsType();
        tvHeader1.setText(newsHeader.getTitle());
        tvHeader2.setText(newsHeader.getDescription());
        List<News> tail = new ArrayList<>();
        if (newsHeader.getNewsList().getData().size() > 0) {
            tail = newsHeader.getNewsList().getData().subList(0, newsHeader.getNewsList().getData().size());
        }
        layoutManager = new InnerLayoutManager();
        headerRecyclerView.setLayoutManager(layoutManager);
        ((NewsBodyAdapter) headerRecyclerView.getAdapter()).addData(tail, newsHeader.getNewsType());
    }

    void clearContent() {
        ((NewsBodyAdapter) headerRecyclerView.getAdapter()).clearData();
    }

    private float computeRatio(RecyclerView recyclerView) {
        final View child0 = recyclerView.getChildAt(0);
        final int pos = recyclerView.getChildAdapterPosition(child0);
        if (pos != 0) {
            return 0;
        }
        final int height = child0.getHeight();
        final float y = Math.max(0, child0.getY());
        return y / height;
    }

    private void onItemScrolled(RecyclerView recyclerView) {
        final float ratio = computeRatio(recyclerView);
        final float answerRatio = Math.max(0, Math.min(ANSWER_RATIO_START, ratio) - ANSWER_RATIO_DIFF) / ANSWER_RATIO_MAX;
        final float middleRatio = Math.max(0, Math.min(MIDDLE_RATIO_START, ratio) - MIDDLE_RATIO_DIFF) / MIDDLE_RATIO_MAX;
        ViewCompat.setAlpha(tvHeader1, answerRatio);
        ViewCompat.setAlpha(tvHeader2, 1f - answerRatio);
        final ViewGroup.LayoutParams lp = clHeader.getLayoutParams();
        lp.height = headerHeight - (int) (view.getResources().getDimensionPixelSize(R.dimen.dp10) * (1f - middleRatio));
        clHeader.setLayoutParams(lp);
        refreshLayout.setEnableRefresh(isTop());
        refreshLayout.setEnableLoadMore(isBottom());
    }

    private boolean isBottom() {
        View lastChildView = layoutManager.getChildAt(headerRecyclerView.getLayoutManager().getChildCount() - 1);
        int lastChildBottom = lastChildView.getBottom() + DpUtil.dp2Int(headerRecyclerView.getContext(), 12);
        int recyclerBottom = headerRecyclerView.getBottom() - headerRecyclerView.getPaddingBottom();
        int lastPosition = layoutManager.getPosition(lastChildView);
        if (lastChildBottom == recyclerBottom && lastPosition == layoutManager.getItemCount() - 1) {
            return true;
        }
        return false;
    }

    private boolean isTop() {
        int topRowVerticalPosition = (headerRecyclerView == null || headerRecyclerView.getChildCount() == 0) ? 0 : headerRecyclerView
                .getChildAt(0).getTop();
        if (topRowVerticalPosition >= headerHeight + DpUtil.dp2Int(view.getContext(), 12)) {
            return true;
        }
        return false;
    }

    @Override
    public View getHeader() {
        return clHeader;
    }

    @Override
    public View getHeaderAlphaView() {
        return headAlpha;
    }

    private void setInformation(int pageIndex) {
        crawlerService.getInformation(CrawlerService.ALL, LanguageUtil.getSettingLanguage(view.getContext()), pageIndex, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<NewsPageWrapper>() {

                    @Override
                    public void onCompleted() {
                        if (index == 0) {
                            refreshLayout.finishRefresh();
                        } else {
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (index == 0) {
                            refreshLayout.finishRefresh();
                        } else {
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(NewsPageWrapper newsPageWrapper) {
                        if (index == 0) {
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).clearData();
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).addData(newsPageWrapper.getData(), NewsHeader.NewsType.NEWS_INFO);
                            refreshLayout.finishRefresh();
                        } else {
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).addData(newsPageWrapper.getData(), NewsHeader.NewsType.NEWS_INFO);
                            headerRecyclerView.scrollBy(0, 200);
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }
                });
    }

    private void setFlash(int pageIndex) {
        crawlerService.getFlash(CrawlerService.ALL, LanguageUtil.getSettingLanguage(view.getContext()), pageIndex, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<NewsPageWrapper>() {
                    @Override
                    public void onCompleted() {
                        if (index == 0) {
                            refreshLayout.finishRefresh();
                        } else {
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (index == 0) {
                            refreshLayout.finishRefresh();
                        } else {
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(NewsPageWrapper newsPageWrapper) {
                        if (index == 0) {
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).clearData();
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).addData(newsPageWrapper.getData(), NewsHeader.NewsType.NEWS_FLASH);
                            refreshLayout.finishRefresh();
                        } else {
                            ((NewsBodyAdapter) headerRecyclerView.getAdapter()).addData(newsPageWrapper.getData(), NewsHeader.NewsType.NEWS_FLASH);
                            headerRecyclerView.scrollBy(0, 200);
                            refreshLayout.finishLoadMore();
                        }
                        unsubscribe();
                    }
                });
    }
}