/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-13 下午3:47
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.walletsdk.manager;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.NumberUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GasDataManager {

    private static BalanceDataManager manager;

    private static GasDataManager gasDataManager = null;

    private Context context;

    private List<GasLimit> gasLimits;

    private BigDecimal recommendGasPrice; // in wei

    private BigDecimal customizeGasPrice; // in wei

    private Observable<BigDecimal> gasObservable;

    private LoopringService loopringService = new LoopringService();

    private GasDataManager(Context context) {
        this.context = context;
        this.loadGasLimitsFromJson();
        manager = BalanceDataManager.getInstance(context);
    }

    public static GasDataManager getInstance(Context context) {
        if (gasDataManager == null) {
            gasDataManager = new GasDataManager(context);
        }
        gasDataManager.getGasPriceFromRelay();
        return gasDataManager;
    }

    // get sellPrice limit from local json file
    private void loadGasLimitsFromJson() {
        try {
            InputStream is = context.getAssets().open("json/gas_limit.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            parseJsonString(new String(buffer, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseJsonString(String jsonData) {
        Gson gson = new Gson();
        this.gasLimits = gson.fromJson(jsonData, new TypeToken<List<GasLimit>>() {
        }.getType());
    }

    // get recommend gas sellPrice through relay
    private void getGasPriceFromRelay() {
        if (this.gasObservable == null) {
            this.gasObservable = loopringService.getEstimateGasPrice()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> {
                        BigInteger bigInteger = Numeric.toBigInt(Numeric.cleanHexPrefix(result));
                        return new BigDecimal(bigInteger);
                    });
        }
        this.gasObservable.subscribe(gasPrice -> {
            recommendGasPrice = gasPrice;
        });
    }

    public void setRecommendGasPrice(BigDecimal recommendGasPrice) {
        this.recommendGasPrice = recommendGasPrice;
    }

    public Observable<BigDecimal> getGasObservable() {
        return gasObservable;
    }

    public BigDecimal getRecommendGasPriceInWei() {
        return this.recommendGasPrice;
    }

    public BigDecimal getRecommendGasPriceInGWei() {
        return Convert.fromWei(this.recommendGasPrice, Convert.Unit.GWEI);
    }

    public BigDecimal getCustomizeGasPriceInEth() {
        return Convert.fromWei(this.customizeGasPrice == null ? recommendGasPrice : customizeGasPrice, Convert.Unit.ETHER);
    }

    public void setCustomizeGasPriceInEth(Double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        this.customizeGasPrice = Convert.toWei(decimal, Convert.Unit.ETHER);
    }

    public String getRecommendGasPriceString() {
        BigDecimal ether = Convert.fromWei(this.recommendGasPrice, Convert.Unit.ETHER);
        int precision = manager.getPrecisionBySymbol("ETH");
        return NumberUtils.format1(ether.doubleValue(), precision);
    }

    public BigDecimal getCustomizeGasPriceInWei() {
        return this.customizeGasPrice == null ? recommendGasPrice : customizeGasPrice;
    }

    public BigDecimal getCustomizeGasPriceInGWei() {
        return Convert.fromWei(this.customizeGasPrice == null ? recommendGasPrice : customizeGasPrice, Convert.Unit.GWEI);
    }

    // ok
    public void setCustomizeGasPriceInGWei(Double value) {
        BigDecimal decimal = BigDecimal.valueOf(value);
        this.customizeGasPrice = Convert.toWei(decimal, Convert.Unit.GWEI);
    }

    public String getCustomizeGasPriceString() {
        BigDecimal ether = Convert.fromWei(this.customizeGasPrice, Convert.Unit.ETHER);
        int precision = manager.getPrecisionBySymbol("ETH");
        return NumberUtils.format1(ether.doubleValue(), precision);
    }

    // ok
    public void setCustomizeGasPriceString(String value) {
        this.customizeGasPrice = Convert.toWei(value, Convert.Unit.WEI);
    }

    public double getGasPriceInWei() {
        BigDecimal price = this.customizeGasPrice == null ? recommendGasPrice : customizeGasPrice;
        return price.doubleValue();
    }

    public double getGasPriceInGwei() {
        return Convert.fromWei(this.customizeGasPrice == null ? recommendGasPrice : customizeGasPrice, Convert.Unit.GWEI)
                .doubleValue();
    }

    public BigDecimal getWeiFromGwei(double gwei) {
        return Convert.toWei(String.valueOf(gwei), Convert.Unit.GWEI);
    }

    public String getGasPriceString() {
        double gasPrice = getGasPriceInWei();
        int precision = manager.getPrecisionBySymbol("ETH");
        return NumberUtils.format1(gasPrice, precision);
    }

    public BigInteger getGasLimitByType(String type) {
        BigInteger result = null;
        for (GasLimit gasLimit : gasLimits) {
            if (gasLimit.type.equalsIgnoreCase(type)) {
                result = gasLimit.gasLimit;
            }
        }
        return result;
    }

    public Double getGasAmountInETH(String type) {
        Double result = null;
        if (getGasLimitByType(type) != null) {
            Double price = getGasPriceInWei();
            BigInteger limit = getGasLimitByType(type);
            result = price * limit.intValue();
        }
        return Convert.fromWei(new BigDecimal(result), Convert.Unit.ETHER).doubleValue();
    }

    public String getGasAmountInETH(String gasLimit, String gasPriceInWei) {
        BigDecimal gasPriceInETH = Convert.fromWei(gasPriceInWei, Convert.Unit.ETHER);
        double limit = Double.parseDouble(gasLimit);
        double price = gasPriceInETH.doubleValue();
        int precision = manager.getPrecisionBySymbol("ETH");
        String result = NumberUtils.format1(limit * price, precision);
        return result;
    }

    public String getGasAmountString(String type) {
        String result = null;
        if (getGasLimitByType(type) != null) {
            Double amount = getGasPriceInWei() * getGasLimitByType(type).intValue();
            int precision = manager.getPrecisionBySymbol("ETH");
            result = NumberUtils.format1(amount, precision);
        }
        return result;
    }

    @SuppressLint("DefaultLocale")
    public String description(String type) {
        return String.format("GasPrice: %s * GasLimit: %6d = %s ETH", getGasPriceString(), getGasLimitByType(type), getGasAmountString(type));
    }

    static class GasLimit {

        String type;

        BigInteger gasLimit;
    }
}
