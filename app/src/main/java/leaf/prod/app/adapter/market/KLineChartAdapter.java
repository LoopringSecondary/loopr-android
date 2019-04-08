package leaf.prod.app.adapter.market;

import java.util.ArrayList;
import java.util.List;

import com.github.fujianlian.klinechart.BaseKLineChartAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-04-04 11:06 AM
 * Cooperation: loopring.org 路印协议基金会
 */
public class KLineChartAdapter extends BaseKLineChartAdapter {

    private List<KLineEntity> datas = new ArrayList<>();

    public KLineChartAdapter() {

    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public String getDate(int position) {
        return datas.get(position).getDate();
    }

    /**
     * 向头部添加数据
     */
    public void addHeaderData(List<KLineEntity> data) {
        if (data != null && !data.isEmpty()) {
            datas.clear();
            datas.addAll(data);
        }
    }

    /**
     * 向尾部添加数据
     */
    public void addFooterData(List<KLineEntity> data) {
        if (data != null && !data.isEmpty()) {
            datas.clear();
            datas.addAll(0, data);
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

    public void setNewDatas(List<KLineEntity> data) {
        if (data != null && !data.isEmpty()) {
            datas.clear();
            datas.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 数据清除
     */
    public void clearData() {
        datas.clear();
        notifyDataSetChanged();
    }
}
