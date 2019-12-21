package leaf.prod.app.adapter.infomation;

import java.util.List;

import android.app.Activity;
import androidx.appcompat.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramotion.garlandview.TailAdapter;

import leaf.prod.app.R;
import leaf.prod.walletsdk.model.NewsHeader;
import leaf.prod.walletsdk.model.response.crawler.BlogWrapper;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-22 4:24 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class NewsHeaderAdapter extends TailAdapter<NewsHeaderItem> {

    private final int POOL_SIZE = 16;

    private final List<NewsHeader> mData;

    private final BlogWrapper blogWrapper;

    private final RecyclerView.RecycledViewPool mPool;

    private Activity activity;

    public NewsHeaderAdapter(BlogWrapper blogWrapper, List<NewsHeader> data, Activity activity) {
        this.blogWrapper = blogWrapper;
        this.mData = data;
        this.activity = activity;
        mPool = new RecyclerView.RecycledViewPool();
        mPool.setMaxRecycledViews(0, POOL_SIZE);
    }

    @Override
    public NewsHeaderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new NewsHeaderItem(view, mPool, activity, blogWrapper);
    }

    @Override
    public void onBindViewHolder(NewsHeaderItem holder, int position) {
        holder.setContent(mData.get(position));
    }

    @Override
    public void onViewRecycled(NewsHeaderItem holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.news_header_item;
    }
}
