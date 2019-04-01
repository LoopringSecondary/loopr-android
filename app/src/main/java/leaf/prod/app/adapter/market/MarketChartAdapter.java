package leaf.prod.app.adapter.market;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.tifezh.kchartlib.chart.BaseKChartAdapter;

import leaf.prod.walletsdk.util.DateUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-03-21 2:14 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class MarketChartAdapter extends BaseKChartAdapter {

    private List<KLineEntity> datas = new ArrayList<>();

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public Date getDate(int position) {
        try {
            return DateUtil.formateDate(datas.get(position).getDate());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 向头部添加数据
     */
    public void addHeaderData(List<KLineEntity> data) {
        if (data != null && !data.isEmpty()) {
            datas.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 向尾部添加数据
     */
    public void addFooterData(List<KLineEntity> data) {
        if (data != null && !data.isEmpty()) {
            datas.addAll(0, data);
            notifyDataSetChanged();
        }
    }

    /**
     * 改变某个点的值
     *
     * @param position 索引值
     */
    public void changeItem(int position, KLineEntity data) {
        datas.set(position, data);
        notifyDataSetChanged();
    }
}
