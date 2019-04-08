package leaf.prod.app.adapter.market;

import java.util.Date;

import com.github.fujianlian.klinechart.entity.IKLine;

import leaf.prod.walletsdk.model.Trend;
import leaf.prod.walletsdk.util.DateUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-03-21 2:18 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class KLineEntity implements IKLine {

    public String Date;

    public float Open;

    public float High;

    public float Low;

    public float Close;

    public float Volume;

    public float MA5Price;

    public float MA10Price;

    public float MA20Price;

    public float MA30Price;

    public float MA60Price;

    public float dea;

    public float dif;

    public float macd;

    public float k;

    public float d;

    public float j;

    public float r;

    public float rsi;

    public float up;

    public float mb;

    public float dn;

    public float MA5Volume;

    public float MA10Volume;

    public static KLineEntity convert(Trend trend) {
        KLineEntity kLineEntity = new KLineEntity();
        kLineEntity.setDate(DateUtil.tempDateMinute(new Date(trend.getStart() * 1000)));
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

    @Override
    public float getMA30Price() {
        return MA30Price;
    }

    @Override
    public float getMA60Price() {
        return MA60Price;
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

    public void setMA30Price(float MA30Price) {
        this.MA30Price = MA30Price;
    }

    public void setMA60Price(float MA60Price) {
        this.MA60Price = MA60Price;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setRsi(float rsi) {
        this.rsi = rsi;
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
                ", MA30Price=" + MA30Price +
                ", MA60Price=" + MA60Price +
                ", dea=" + dea +
                ", dif=" + dif +
                ", macd=" + macd +
                ", k=" + k +
                ", d=" + d +
                ", j=" + j +
                ", r=" + r +
                ", rsi=" + rsi +
                ", up=" + up +
                ", mb=" + mb +
                ", dn=" + dn +
                ", MA5Volume=" + MA5Volume +
                ", MA10Volume=" + MA10Volume +
                '}';
    }

    @Override
    public float getRsi() {
        return 0;
    }

    @Override
    public float getR() {
        return 0;
    }
}
