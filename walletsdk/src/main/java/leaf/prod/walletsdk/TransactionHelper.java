//package leaf.prod.walletsdk;
//
//import java.math.BigInteger;
//
//import org.web3j.tx.ChainId;
//import org.web3j.utils.Numeric;
//import com.google.common.base.Strings;
//import leaf.prod.walletsdk.exception.TransactionException;
//import leaf.prod.walletsdk.model.TransactionObject;
//import leaf.prod.walletsdk.service.EthereumService;
//import leaf.prod.walletsdk.service.LoopringService;
//import leaf.prod.walletsdk.util.SignUtils;
//
//public class TransactionHelper {
//
//    private static LoopringService loopringService = new LoopringService();
//
//    private static EthereumService ethereumService = new EthereumService();
//
//    public static TransactionObject createTransaction(String from, String to, BigInteger weiValue) {
//        return createTransaction(from, to, weiValue, "");
//    }
//
//    public static TransactionObject createTransaction(String from, String to, BigInteger weiValue, String data) {
//        String nonceStr = loopringService.getNonce(from).toBlocking().single();
//        String gasPriceStr = loopringService.getEstimateGasPrice().toBlocking().single();
//        BigInteger nonce = Numeric.toBigInt(Numeric.cleanHexPrefix(nonceStr));
//        BigInteger gasPrice = Numeric.toBigInt(Numeric.cleanHexPrefix(gasPriceStr));
//        TransactionObject transactionObject = createTransaction(ChainId.MAINNET, from, to, nonce, gasPrice, BigInteger.ZERO, weiValue, data);
//        BigInteger estimateGasLimit = ethereumService.estimateGasLimit(transactionObject);
//        transactionObject.setGasLimit(estimateGasLimit);
//        return transactionObject;
//    }
//
//    public static TransactionObject createTransaction(byte chainId, String from, String to, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimited, BigInteger weiValue, String data) {
//        if (Strings.isNullOrEmpty(data)) {
//            data = "0x";
//        } else {
//            data = Numeric.toHexString(data.getBytes());
//        }
//        return new TransactionObject(
//                chainId,
//                from,
//                to,
//                nonce,
//                gasPrice,
//                gasLimited,
//                weiValue,
//                data
//        );
//    }
//
//    @Deprecated
//    public static String sendTransaction(TransactionObject transactionObject, String privateKey) throws TransactionException {
//        String signedTransaction = SignUtils.signTransaction(transactionObject, privateKey);
//        String txHash = ethereumService.sendRawTransaction(signedTransaction);
//        String txHashReply = loopringService.notifyTransactionSubmitted(txHash, transactionObject)
//                .toBlocking()
//                .single();
//        if (txHash.equals(txHashReply)) {
//            return txHash;
//        } else {
//            throw new TransactionException("relay notification failure.");
//        }
//    }
//}