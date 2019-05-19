/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-09-13 下午1:55
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.walletsdk.manager;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import org.web3j.utils.Convert;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.walletsdk.model.response.relay.Token;
import leaf.prod.walletsdk.service.LoopringService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TokenDataManager {

    private static TokenDataManager tokenDataManager = null;

    private List<Token> tokens;

    private List<Token> whiteList;

    private Context context;

    private Observable<List<Token>> tokenObservable;

    private LoopringService loopringService = new LoopringService();

    private TokenDataManager(Context context) {
        this.context = context;
        this.loadTokensFromLocal();
        this.loadTokensFromRelay();
    }

    public static TokenDataManager getInstance(Context context) {
        if (tokenDataManager == null) {
            tokenDataManager = new TokenDataManager(context);
        }
        return tokenDataManager;
    }

    // Generate whitelist of tokens through tokens local json file
    private void loadTokensFromLocal() {
        try {
            InputStream is = context.getAssets().open("json/tokens.json");
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
        List<Token> tokens = gson.fromJson(jsonData, new TypeToken<List<Token>>() {
        }.getType());
        this.tokens = tokens;
        this.whiteList = tokens;
    }

    private void loadTokensFromRelay() {
        String owner = WalletUtil.getCurrentAddress(context);
        if (this.tokenObservable == null) {
            this.tokenObservable = loopringService
                    .getCustomToken(owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public List<Token> getTokens() {
        return this.tokens;
    }

    public List<Token> getERC20Tokens() {
        return getTokensExcept("ETH");
    }

    public List<Token> getTokensExcept(String symbol) {
        List<Token> result = new ArrayList<>(tokens);
        for (Token token : result) {
            if (token.getSymbol().equalsIgnoreCase(symbol)) {
                result.remove(token);
                break;
            }
        }
        return result;
    }

    public List<Token> getWhiteList() {
        return this.whiteList;
    }

    public Observable<List<Token>> getTokenObservable() {
        return tokenObservable;
    }

    public void addToken(Token token) {
        if (!tokens.contains(token)) {
            this.tokens.add(token);
        }
    }

    public Token getTokenBySymbol(String symbol) {
        Token result = null;
        for (Token token : this.tokens) {
            if (token.getSymbol().equalsIgnoreCase(symbol)) {
                result = token;
                break;
            }
        }
        return result;
    }

    public Token getTokenByProtocol(String protocol) {
        Token result = null;
        for (Token token : this.tokens) {
            if (token.getProtocol().equalsIgnoreCase(protocol)) {
                result = token;
                break;
            }
        }
        return result;
    }

    public Double getDoubleFromWei(String symbol, BigDecimal valueInWei) {
        Double result = null;
        Token token = getTokenBySymbol(symbol);
        if (token != null) {
            BigDecimal decimals = token.getDecimals();
            result = valueInWei.divide(decimals).doubleValue();
        }
        return result;
    }

    public Double getDoubleFromWei(String symbol, String valueInWei) {
        try {
            BigDecimal bigDecimal = Convert.toWei(valueInWei, Convert.Unit.WEI);
            return getDoubleFromWei(symbol, bigDecimal);
        } catch (Exception e) {
            return 0d;
        }
    }

    public BigInteger getWeiFromDouble(String symbol, String doubleString) {
        Token token = getTokenBySymbol(symbol);
        return new BigDecimal(doubleString).multiply(token.getDecimals()).toBigInteger();
    }

    public BigInteger getWeiFromDouble(String symbol, Double doubleValue) {
        Token token = getTokenBySymbol(symbol);
        return new BigDecimal(doubleValue).multiply(token.getDecimals()).toBigInteger();
    }

    // support for main fragment presenter
    public void mergeTokens(List<Token> tokens) {
        synchronized (this) {
            List<String> chooseList = WalletUtil.getChooseTokens(context);
            List<Token> tTokens = new ArrayList<>();
            Map<String, Token> map = new HashMap<>();
            for (Token token : tokens) {
                addToken(token);
            }
            for (Token token : this.tokens) {
                String image = String.format("icon_token_%s", token.getSymbol().toLowerCase());
                int identifier = context.getResources().getIdentifier(image, "mipmap", context.getPackageName());
                token.setImageResId(identifier);
                map.put(token.getSymbol(), token);
            }
            Collections.sort(this.tokens, (t1, t2) -> t1.getSymbol().compareTo(t2.getSymbol()));
            tTokens.add(map.get("ETH"));
            tTokens.add(map.get("WETH"));
            tTokens.add(map.get("LRC"));
            if (chooseList != null) {
                Collections.sort(chooseList, String::compareTo);
                for (String symbol : chooseList) {
                    if (!tTokens.contains(map.get(symbol))) {
                        tTokens.add(map.get(symbol));
                    }
                }
            }
            for (Token token : this.tokens) {
                if (!tTokens.contains(token)) {
                    tTokens.add(token);
                }
            }
            this.tokens = tTokens;
        }
    }

    public static Token getToken(String symbol) {
        return tokenDataManager.getTokenBySymbol(symbol);
    }

    public static Double getDouble(String symbol, String valueInWei) {
        return tokenDataManager.getDoubleFromWei(symbol, valueInWei);
    }
}
