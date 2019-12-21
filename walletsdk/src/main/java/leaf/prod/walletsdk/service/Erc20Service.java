package leaf.prod.walletsdk.service;

import java.util.Arrays;
import java.util.Collections;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

import leaf.prod.walletsdk.Default;
import leaf.prod.walletsdk.SDK;
import rx.Observable;

public class Erc20Service {

    private Web3j web3j;

    public Erc20Service() {
        String ethBase = SDK.ethBase();
        HttpService httpService = new HttpService(ethBase);
        web3j = Web3j.build(httpService);
    }

    public Observable<EthCall> getBindAddress(String owner, int projectId) {
        String encode = FunctionEncoder.encode(getBindFunction(owner, projectId));
        Transaction ethCallTransaction = Transaction.createEthCallTransaction( owner, Default.BIND_CONTRACT, encode );
        return web3j.ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST).observable();
    }

    public Function getBindFunction(String owner, int projectId) {
        return new Function("getBindingAddress",
                Arrays.asList(new Address(owner), new Uint8(projectId)),
                Collections.singletonList(new TypeReference<Utf8String>() {}));
    }
}
