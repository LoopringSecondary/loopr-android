package leaf.prod.app.views;

import android.content.Context;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by niedengqiang on 2018/8/29.
 */

public class RecyclerViewBugLayoutManager extends LinearLayoutManager {

    public RecyclerViewBugLayoutManager(Context context) {
        super(context);
    }

    public RecyclerViewBugLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public RecyclerViewBugLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //    @Override
    //    public boolean supportsPredictiveItemAnimations() {
    //        return false;
    //    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            //把异常抛出来，不至于程序崩溃
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //    @Override
    //    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    //        try {
    //            return super.scrollVerticallyBy(dy, recycler, state);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return 0;
    //    }
}
