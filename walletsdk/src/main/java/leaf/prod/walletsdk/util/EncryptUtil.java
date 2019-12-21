package leaf.prod.walletsdk.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-10 3:52 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class EncryptUtil {

    public static String encryptSHA3(String pwd) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        return MyBase64.encode(digestSHA3.digest(pwd.getBytes()));
    }

    public static String encryptSHA256(String pwd, String random) {
        try {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
            gen.init(pwd.getBytes(), MyBase64.decode(random), 4096);
            return MyBase64.encode(((KeyParameter) gen.generateDerivedParameters(256)).getKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSecureRandom() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return MyBase64.encode(salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encryptAES(String content, String pwd, String random) {
        try {
            SecretKeySpec key = new SecretKeySpec(MyBase64.decode(pwd), "AES");
            IvParameterSpec iv = new IvParameterSpec(MyBase64.decode(random));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return MyBase64.encode(cipher.doFinal(content.getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String decryptAES(String content, String pwd, String random) {
        try {
            SecretKeySpec key = new SecretKeySpec(MyBase64.decode(pwd), "AES");
            IvParameterSpec iv = new IvParameterSpec(MyBase64.decode(random));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return new String(cipher.doFinal(MyBase64.decode(content)));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        String psw = "111111";
        String salt = getSecureRandom(), iv = getSecureRandom();
        String sha256 = encryptSHA256(psw, salt);
        String en = encryptAES(" 1 2 3 4 5 6", sha256, iv);
        System.out.println(en);
        System.out.println(decryptAES(en, sha256, iv));
    }
}
