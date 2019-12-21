package leaf.prod.app.layout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.appcompat.widget.PagerSnapHelper;
import androidx.appcompat.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-01-04 8:19 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class RLoopRecyclerView extends RecyclerView {

    private static final String TAG = "angcyo";

    public RLoopRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RLoopRecyclerView(Context context) {
        super(context);
    }

    public RLoopRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initView();
    }

    @Override
    public LoopAdapter getAdapter() {
        return (LoopAdapter) super.getAdapter();
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof LoopAdapter)) {
            throw new IllegalArgumentException("adapter must  instanceof LoopAdapter!");
        }
        super.setAdapter(adapter);
        scrollToPosition(getAdapter().getItemRawCount() * 10000);//开始时的偏移量
    }

    private void initView() {
        new RPagerSnapHelper().setOnPageListener(new RPagerSnapHelper.OnPageListener() {
            @Override
            public void onPageSelector(int position) {
                Log.e(TAG, "onPageSelector: " + position % getAdapter().getItemRawCount());
            }
        }).attachToRecyclerView(this);
    }

    public static abstract class LoopAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

        List<String> datas = new ArrayList<>();

        public void setDatas(List<String> datas) {
            this.datas = datas;
            notifyDataSetChanged();
        }

        /**
         * 真实数据的大小
         */
        public int getItemRawCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        final public int getItemViewType(int position) {
            return getLoopItemViewType(position % getItemRawCount());
        }

        protected int getLoopItemViewType(int position) {
            return 0;
        }

        @Override
        final public void onBindViewHolder(T holder, int position) {
            onBindLoopViewHolder(holder, position % getItemRawCount());
        }

        public abstract void onBindLoopViewHolder(T holder, int position);

        @Override
        final public int getItemCount() {
            int rawCount = getItemRawCount();
            if (rawCount > 0) {
                return Integer.MAX_VALUE;
            }
            return 0;
        }
    }

    static class RPagerSnapHelper extends PagerSnapHelper {

        OnPageListener mOnPageListener;

        int mCurrentPosition = 0;

        public int getCurrentPosition() {
            return mCurrentPosition;
        }

        public RPagerSnapHelper setCurrentPosition(int currentPosition) {
            mCurrentPosition = currentPosition;
            return this;
        }

        public OnPageListener getOnPageListener() {
            return mOnPageListener;
        }

        /**
         * 页面选择回调监听
         */
        public RPagerSnapHelper setOnPageListener(OnPageListener onPageListener) {
            mOnPageListener = onPageListener;
            return this;
        }

        @Override
        public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
            super.attachToRecyclerView(recyclerView);
            if (recyclerView != null) {
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            //开始滚动
                        } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            //结束滚动
                        } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                            //滑行中
                        }
                    }
                });
            }
        }

        @Nullable
        @Override
        public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) targetView.getLayoutParams();
            int position = params.getViewAdapterPosition();
            int left = targetView.getLeft();
            int right = targetView.getRight();
            ViewGroup viewGroup = (ViewGroup) targetView.getParent();
            int[] out = new int[]{0, 0};
            boolean isLastItem = position == layoutManager.getItemCount() - 1/*最后一个*/ && right == viewGroup.getMeasuredWidth();
            //        if (left == 0 || isLastItem) {
            //            return out;
            //        }
            out[0] = left;
            out[1] = 0;
            if (mOnPageListener != null && mCurrentPosition != position && (out[0] == 0 || isLastItem)) {
                mOnPageListener.onPageSelector(mCurrentPosition = position);
            }
            return out;
        }

        public interface OnPageListener {

            void onPageSelector(int position);
        }
    }
}
