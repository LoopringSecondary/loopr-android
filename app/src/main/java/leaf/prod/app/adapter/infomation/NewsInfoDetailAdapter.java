package leaf.prod.app.adapter.infomation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.PagerSnapHelper;
import androidx.appcompat.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.loopj.android.image.WebImage;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import leaf.prod.app.R;
import leaf.prod.app.layout.RoundSmartImageView;
import leaf.prod.walletsdk.manager.NewsDataManager;
import leaf.prod.walletsdk.model.response.crawler.News;
import leaf.prod.walletsdk.util.DpUtil;

public class NewsInfoDetailAdapter extends BaseQuickAdapter<News, BaseViewHolder> {

    private RecyclerView recyclerView;

    private static int margin = 0;

    private List<News> newsList;

    private NewsDataManager newsDataManager;

    private int index = 1;

    private LinearLayoutManager layoutManager;

    private PagerSnapHelper pagerSnapHelper;

    private static int animate = 0;

    public NewsInfoDetailAdapter(int layoutResId, List<News> news, int index, RecyclerView recyclerView) {
        super(layoutResId, news);
        this.newsList = news;
        this.index = index;
        this.recyclerView = recyclerView;
        layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        this.recyclerView.setLayoutManager(layoutManager);
        pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(this.recyclerView);
        layoutManager.scrollToPositionWithOffset(index, 0);
        margin = DpUtil.dp2Int(recyclerView.getContext(), 12);
        newsDataManager = NewsDataManager.getInstance(recyclerView.getContext());
    }

    @Override
    protected void convert(BaseViewHolder helper, News item) {
        if (item == null)
            return;
        ((LinearLayout) helper.getView(R.id.ll_content)).removeAllViews();
        helper.setText(R.id.tv_title, item.getTitle());
        helper.setText(R.id.tv_time, item.getPublishTime());
        helper.setText(R.id.tv_source, recyclerView.getContext().getResources()
                .getString(R.string.news_source) + ": " + item.getSource());
        Pattern p = Pattern.compile("<img src=\"([\\s\\S]*?)\">");
        Matcher m = p.matcher(item.getContent());
        int begin = 0;
        int end = 0;
        while (m.find()) {
            String content = item.getContent().substring(begin, m.start());
            String image = item.getContent()
                    .substring(m.start(), m.end())
                    .replace("<img src=\"", "")
                    .replace("\">", "");
            addTextView(helper, content.trim());
            addImageView(helper, image);
            begin = end = m.end();
        }
        if (begin == 0) {
            addTextView(helper, item.getContent());
        }
        if (end < item.getContent().length() && !item.getContent().substring(end).trim().isEmpty()) {
            addTextView(helper, item.getContent().substring(end).trim());
        }
        ScrollView svContent = helper.getView(R.id.sv_content);
        svContent.post(() -> svContent.scrollTo(0, 0));
        svContent.scrollTo(0, 0);
        ((SmartRefreshLayout) helper.getView(R.id.refresh_layout)).setOnRefreshListener(refreshLayout -> {
            if (index > 0) {
                goPre();
                svContent.startAnimation(AnimationUtils.loadAnimation(recyclerView.getContext(), R.anim.translate_between_interface_bottom_in));
                layoutManager.scrollToPositionWithOffset(index, 0);
                refreshLayout.finishRefresh(true);
            } else {
                refreshLayout.finishRefresh(false);
            }
        });
        ((SmartRefreshLayout) helper.getView(R.id.refresh_layout)).setOnLoadMoreListener(refreshLayout -> {
            if (index < newsList.size() - 1) {
                goNext();
                svContent.startAnimation(AnimationUtils.loadAnimation(recyclerView.getContext(), R.anim.translate_between_interface_top_in));
                layoutManager.scrollToPositionWithOffset(index, 0);
                refreshLayout.finishLoadMore();
            } else {
                refreshLayout.finishLoadMoreWithNoMoreData();
            }
        });
        if (animate == 1) {
            svContent.startAnimation(AnimationUtils.loadAnimation(recyclerView.getContext(), R.anim.translate_between_interface_top_in));
        } else if (animate == 2) {
            svContent.startAnimation(AnimationUtils.loadAnimation(recyclerView.getContext(), R.anim.translate_between_interface_bottom_in));
        }
        animate = 0;
    }

    private void goPre() {
        if (index > 1) {
            index--;
        } else {
            News preNews = newsDataManager.getPreNews(newsList.get(0));
            if (preNews == null) {
                index = 0;
            } else {
                newsList.add(0, preNews);
                notifyItemInserted(0);
            }
        }
        animate = 1;
        notifyDataSetChanged();
    }

    private void goNext() {
        if (index < newsList.size() - 2) {
            index++;
        } else {
            News nextNews = newsDataManager.getNextNews(newsList.get(newsList.size() - 1));
            if (nextNews != null) {
                newsList.add(nextNews);
                notifyItemInserted(newsList.size() - 1);
            } else {
            }
            index++;
        }
        animate = 2;
        notifyDataSetChanged();
    }

    private void addTextView(BaseViewHolder holder, String content) {
        if (content.trim().isEmpty())
            return;
        TextView textView = new TextView(recyclerView.getContext());
        textView.setId(View.generateViewId());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, margin, 0, 0);
        textView.setLayoutParams(lp);
        textView.setTextIsSelectable(true);
        textView.setText(content);
        textView.setTextColor(recyclerView.getContext().getResources().getColor(R.color.colorNineText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setLineSpacing(0, 1.5f);
        ((LinearLayout) holder.getView(R.id.ll_content)).addView(textView);
    }

    private void addImageView(BaseViewHolder holder, String url) {
        WebImage webImage = new WebImage(url);
        RoundSmartImageView imageView = new RoundSmartImageView(recyclerView.getContext());
        imageView.setId(View.generateViewId());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, margin, 0, 0);
        imageView.setLayoutParams(lp);
        imageView.setImage(webImage);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        ((LinearLayout) holder.getView(R.id.ll_content)).addView(imageView);
    }
}
