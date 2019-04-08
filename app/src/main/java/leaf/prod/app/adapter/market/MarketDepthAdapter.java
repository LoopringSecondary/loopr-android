package leaf.prod.app.adapter.market;

import java.util.List;

import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import leaf.prod.app.R;
import leaf.prod.walletsdk.model.MarketDepthItem;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.StringUtils;

public class MarketDepthAdapter extends BaseQuickAdapter<MarketDepthItem, BaseViewHolder> {

    private String side;

    public MarketDepthAdapter(int layoutResId, @Nullable List<MarketDepthItem> data, String side) {
        super(layoutResId, data);
        this.side = side;
    }

    @Override
    protected void convert(BaseViewHolder helper, MarketDepthItem item) {
        if (item == null || item.getDepths() == null || item.getDepths().length != 3 || StringUtils.isEmpty(side)) {
            return;
        }
        if (StringUtils.isEmpty(item.getDepths()[0]) || StringUtils.isEmpty(item.getDepths()[1])) {
            helper.setText(R.id.tv_price, "");
            helper.setText(R.id.tv_amount, "");
        } else {
            helper.setText(R.id.tv_price, NumberUtils.format1(Double.valueOf(item.getDepths()[0]), 8));
            helper.setText(R.id.tv_amount, NumberUtils.format7(Double.valueOf(item.getDepths()[1]), 0, 2));
            if (side.equals("buy")) {
                if (helper.getView(R.id.ll_left_bg) != null) {
                    helper.setVisible(R.id.ll_left_bg, true);
                    helper.getView(R.id.left_bg1)
                            .setLayoutParams(new LinearLayout.LayoutParams(0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (float) (1 - item.getRate())));
                    helper.getView(R.id.left_bg2)
                            .setLayoutParams(new LinearLayout.LayoutParams(0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (float) item.getRate()));
                }
                helper.setTextColor(R.id.tv_price, mContext.getResources().getColor(R.color.colorGreen));
            } else {
                if (helper.getView(R.id.ll_right_bg) != null) {
                    helper.setVisible(R.id.ll_right_bg, true);
                    helper.getView(R.id.right_bg1)
                            .setLayoutParams(new LinearLayout.LayoutParams(0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (float) (1 - item.getRate())));
                    helper.getView(R.id.right_bg2)
                            .setLayoutParams(new LinearLayout.LayoutParams(0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (float) item.getRate()));
                }
                helper.setTextColor(R.id.tv_price, mContext.getResources().getColor(R.color.colorRed));
            }
        }
    }
}
