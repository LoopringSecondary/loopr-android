package leaf.prod.app.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.loopj.android.image.SmartImageView;

import leaf.prod.walletsdk.util.DpUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-01-02 11:50 AM
 * Cooperation: loopring.org 路印协议基金会
 */
public class RoundSmartImageView extends SmartImageView {

    private float width, height;

    private int corner;

    public RoundSmartImageView(Context context) {
        this(context, null);
        init(context, null);
    }

    public RoundSmartImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public RoundSmartImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        corner = DpUtil.dp2Int(getContext(), 5);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width >= corner && height > corner) {
            Path path = new Path();
            //四个圆角
            path.moveTo(corner, 0);
            path.lineTo(width - corner, 0);
            path.quadTo(width, 0, width, corner);
            path.lineTo(width, height - corner);
            path.quadTo(width, height, width - corner, height);
            path.lineTo(corner, height);
            path.quadTo(0, height, 0, height - corner);
            path.lineTo(0, corner);
            path.quadTo(0, 0, corner, 0);
            canvas.clipPath(path);
        }
        super.onDraw(canvas);
    }
}
