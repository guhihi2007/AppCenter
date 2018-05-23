package cn.lt.framework.crypt;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by wenchao on 2016/1/19.
 */
public class DES {
    public DES() {
    }

    public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
        DESKeySpec       dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey        securekey = keyFactory.generateSecret(dks);
        Cipher           cipher = Cipher.getInstance("DES");
        SecureRandom     sr = new SecureRandom();
        cipher.init(1, securekey, sr);
        return cipher.doFinal(src);
    }

    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        SecureRandom sr = new SecureRandom();
        cipher.init(2, securekey, sr);
        return cipher.doFinal(src);
    }
}
