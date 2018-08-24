package com.tomcat360.lyqb.core;

import com.tomcat360.lyqb.core.service.LooprHttpService;
import com.tomcat360.lyqb.core.singleton.Web3jInstance;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;

public class TransactionHelper {
    private static final Web3j web3j = Web3jInstance.getInstance();

    private static final LooprHttpService httpService = new LooprHttpService("");

//    public static String createEthTrasnferTransaction(String to, Credentials credentials, BigInteger value) {
//
//        long nonce = httpService.getNonce(credentials.getAddress()).toBlocking().first().getNonce();
//        String gasPrice = httpService.getEstimateGasPrice().toBlocking().first().getGasPrice();
//        String s = Numeric.cleanHexPrefix(gasPrice);
//        BigInteger bigInteger = Numeric.toBigInt(s);
//
//        RawTransaction rawTransaction = RawTransaction.createTransaction(
//                BigInteger.valueOf(nonce),
//                bigInteger,
//                new BigInteger("120000000000"),
//                to,
//                value,
//                null
//        );
//
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//        return Numeric.toHexString(signedMessage);
//    }

    public static String sendTransaction(String signedData) throws IOException {
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedData).send();
        if (ethSendTransaction.hasError()) {
            String message = ethSendTransaction.getError().getMessage();
            throw new RuntimeException(message);
        }else {
            return ethSendTransaction.getTransactionHash();
        }
    }
}
