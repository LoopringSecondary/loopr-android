package leaf.prod.app.adapter.market;

import java.util.Date;

import com.github.tifezh.kchartlib.chart.entity.IKLine;

import leaf.prod.walletsdk.model.Trend;
import leaf.prod.walletsdk.util.DateUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-03-21 2:18 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class KLineEntity implements IKLine {

    private String Date;

    private float Open;

    private float High;

    private float Low;

    private float Close;

    private float Volume;

    private float MA5Price;

    private float MA10Price;

    private float MA20Price;

    private float dea;

    private float dif;

    private float macd;

    private float k;

    private float d;

    private float j;

    private float rsi1;

    private float rsi2;

    private float rsi3;

    private float up;

    private float mb;

    private float dn;

    private float MA5Volume;

    private float MA10Volume;

    public static KLineEntity convert(Trend trend) {
        KLineEntity kLineEntity = new KLineEntity();
        kLineEntity.setDate(DateUtil.formatDateDay(new Date(trend.getStart() * 1000)));
        kLineEntity.setOpen(trend.getOpen().floatValue());
        kLineEntity.setHigh(trend.getHigh().floatValue());
        kLineEntity.setLow(trend.getLow().floatValue());
        kLineEntity.setClose(trend.getClose().floatValue());
        kLineEntity.setVolume(trend.getVol().floatValue());
        return kLineEntity;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public float getOpen() {
        return Open;
    }

    public void setOpen(float open) {
        Open = open;
    }

    public float getHigh() {
        return High;
    }

    public void setHigh(float high) {
        High = high;
    }

    public float getLow() {
        return Low;
    }

    public void setLow(float low) {
        Low = low;
    }

    public float getClose() {
        return Close;
    }

    public void setClose(float close) {
        Close = close;
    }

    @Override
    public float getVolume() {
        return Volume;
    }

    public void setVolume(float volume) {
        Volume = volume;
    }

    @Override
    public float getOpenPrice() {
        return Open;
    }

    @Override
    public float getHighPrice() {
        return High;
    }

    @Override
    public float getLowPrice() {
        return Low;
    }

    @Override
    public float getClosePrice() {
        return Close;
    }

    @Override
    public float getMA5Price() {
        return MA5Price;
    }

    public void setMA5Price(float MA5Price) {
        this.MA5Price = MA5Price;
    }

    @Override
    public float getMA10Price() {
        return MA10Price;
    }

    public void setMA10Price(float MA10Price) {
        this.MA10Price = MA10Price;
    }

    @Override
    public float getMA20Price() {
        return MA20Price;
    }

    public void setMA20Price(float MA20Price) {
        this.MA20Price = MA20Price;
    }

    @Override
    public float getDea() {
        return dea;
    }

    public void setDea(float dea) {
        this.dea = dea;
    }

    @Override
    public float getDif() {
        return dif;
    }

    public void setDif(float dif) {
        this.dif = dif;
    }

    @Override
    public float getMacd() {
        return macd;
    }

    public void setMacd(float macd) {
        this.macd = macd;
    }

    @Override
    public float getK() {
        return k;
    }

    public void setK(float k) {
        this.k = k;
    }

    @Override
    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    @Override
    public float getJ() {
        return j;
    }

    public void setJ(float j) {
        this.j = j;
    }

    @Override
    public float getRsi1() {
        return rsi1;
    }

    public void setRsi1(float rsi1) {
        this.rsi1 = rsi1;
    }

    @Override
    public float getRsi2() {
        return rsi2;
    }

    public void setRsi2(float rsi2) {
        this.rsi2 = rsi2;
    }

    @Override
    public float getRsi3() {
        return rsi3;
    }

    public void setRsi3(float rsi3) {
        this.rsi3 = rsi3;
    }

    @Override
    public float getUp() {
        return up;
    }

    public void setUp(float up) {
        this.up = up;
    }

    @Override
    public float getMb() {
        return mb;
    }

    public void setMb(float mb) {
        this.mb = mb;
    }

    @Override
    public float getDn() {
        return dn;
    }

    public void setDn(float dn) {
        this.dn = dn;
    }

    @Override
    public float getMA5Volume() {
        return MA5Volume;
    }

    public void setMA5Volume(float MA5Volume) {
        this.MA5Volume = MA5Volume;
    }

    @Override
    public float getMA10Volume() {
        return MA10Volume;
    }

    public void setMA10Volume(float MA10Volume) {
        this.MA10Volume = MA10Volume;
    }

    @Override
    public String toString() {
        return "KLineEntity{" +
                "Date='" + Date + '\'' +
                ", Open=" + Open +
                ", High=" + High +
                ", Low=" + Low +
                ", Close=" + Close +
                ", Volume=" + Volume +
                ", MA5Price=" + MA5Price +
                ", MA10Price=" + MA10Price +
                ", MA20Price=" + MA20Price +
                ", dea=" + dea +
                ", dif=" + dif +
                ", macd=" + macd +
                ", k=" + k +
                ", d=" + d +
                ", j=" + j +
                ", rsi1=" + rsi1 +
                ", rsi2=" + rsi2 +
                ", rsi3=" + rsi3 +
                ", up=" + up +
                ", mb=" + mb +
                ", dn=" + dn +
                ", MA5Volume=" + MA5Volume +
                ", MA10Volume=" + MA10Volume +
                '}';
    }
}
