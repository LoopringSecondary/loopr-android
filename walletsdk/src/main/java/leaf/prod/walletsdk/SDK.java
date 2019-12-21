package leaf.prod.walletsdk;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;

import io.socket.client.IO;
import io.socket.client.Socket;
import leaf.prod.walletsdk.exception.SdkInitializeException;
import leaf.prod.walletsdk.exception.UninitializedException;
import leaf.prod.walletsdk.util.StringUtils;
import okhttp3.OkHttpClient;

public class SDK {

    public static byte CHAIN_ID = ChainId.NONE;

    private static OkHttpClient okHttpClient = null;

    private static Socket socketClient = null;

    private static String LOOPRING_BASE = "https://relay1.loopring.io";

    private static String ETH_BASE = "https://relay1.loopring.io/eth";

    private static String APP_SERVICE_BASE = "https://www.loopring.mobi";

    private static Web3j web3j = null;

    public static String relayBase() {
        return LOOPRING_BASE;
    }

    public static String ethBase() {
        return ETH_BASE;
    }

    public static String appServiceBase() {
        return APP_SERVICE_BASE;
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            throw new SdkInitializeException();
        }
        return okHttpClient;
    }

    public static Socket getSocketClient() {
        if (okHttpClient == null) {
            throw new SdkInitializeException();
        }
        return socketClient;
    }

    public static void initSDK() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        HttpService httpService = new HttpService(ETH_BASE);
        web3j = Web3j.build(httpService);
        IO.Options opt = new IO.Options();
        opt.reconnection = true;
        opt.reconnectionAttempts = 5;
        opt.transports = new String[]{"websocket"};
        opt.callFactory = okHttpClient;
        opt.webSocketFactory = okHttpClient;
        try {
            socketClient = IO.socket(StringUtils.formatUrlEnding(LOOPRING_BASE), opt);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new UninitializedException();
        }
        socketClient.on(Socket.EVENT_CONNECT, args -> System.out.println("socket connection established!"));
        socketClient.on(Socket.EVENT_CONNECT_ERROR, args -> System.out.println("network error"));
        socketClient.on(Socket.EVENT_CONNECTING, args -> System.out.println("connecting"));
        socketClient.connect();
    }

    public static Web3j getWeb3j() {
        if (web3j == null) {
            throw new UninitializedException();
        }
        return web3j;
    }
}
