package leaf.prod.app.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by niedengqiang on 2018/8/15.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int left;
    private int right;
    private int bottom;
    private int top;

    public SpacesItemDecoration(int left, int right, int bottom, int top) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = left;
        outRect.right = right;
        outRect.bottom = bottom;
        outRect.top = top;
    }
}
