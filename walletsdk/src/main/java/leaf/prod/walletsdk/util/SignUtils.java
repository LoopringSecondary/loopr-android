package leaf.prod.walletsdk.util;

import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import leaf.prod.walletsdk.model.SignedBody;
import leaf.prod.walletsdk.model.request.relayParam.NotifyScanParam;

public class SignUtils {

    public static Sign.SignatureData genSignRawTx(Credentials credentials, RawTransaction rawTransaction) {
        byte[] encodedTransaction = TransactionEncoder.encode(rawTransaction);
        return Sign.signMessage(encodedTransaction, credentials.getEcKeyPair());
    }

    public static SignedBody genSignMessage(Credentials credentials, byte[] hash) {
        byte[] prefix = ("\u0019Ethereum Signed Message:\n" + hash.length).getBytes();
        byte[] finalBytes = new byte[prefix.length + hash.length];
        System.arraycopy(prefix, 0, finalBytes, 0, prefix.length);
        System.arraycopy(hash, 0, finalBytes, prefix.length, hash.length);
        Sign.SignatureData sig = Sign.signMessage(finalBytes, credentials.getEcKeyPair());
        return SignedBody.builder().hash(Numeric.toHexString(hash)).sig(sig).build();
    }

    public static NotifyScanParam.SignParam genSignParam(Credentials credentials, String owner) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        byte[] hash = Hash.sha3(timeStamp.getBytes());
        Sign.SignatureData sig = SignUtils.genSignMessage(credentials, hash).getSig();
        return NotifyScanParam.SignParam.builder().timestamp(timeStamp).owner(owner)
                .r(Numeric.toHexStringNoPrefix(sig.getR()))
                .s(Numeric.toHexStringNoPrefix(sig.getS()))
                .v(BigInteger.valueOf(1L)).build();
    }
}
