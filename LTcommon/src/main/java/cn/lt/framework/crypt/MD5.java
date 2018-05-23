package cn.lt.framework.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.lt.framework.log.Logger;


/**
 * Created by wenchao on 2016/1/19.
 */
public class MD5 {
    private static MessageDigest md5 = null;

    public MD5() {
    }

    private static synchronized MessageDigest getMessageDigest() {
        if (md5 == null) {
            try {
                md5 = MessageDigest.getInstance("md5");
            } catch (NoSuchAlgorithmException var1) {
                Logger.e(var1.getMessage());
                return null;
            }
        }
        return md5;
    }

    public static byte[] encode(InputStream is) {
        MessageDigest md = getMessageDigest();
        if(md == null) {
            throw new IllegalAccessError("no md5 algorithm");
        } else {
            DigestInputStream dis = null;
            byte[] buf = new byte[1024];
            boolean reads = false;

            try {
                dis = new DigestInputStream(is, md);

                int reads1;
                do {
                    reads1 = dis.read(buf);
                } while(reads1 >= 0);
            } catch (IOException var14) {
                ;
            } finally {
                if(dis != null) {
                    try {
                        dis.close();
                    } catch (IOException var13) {
                        ;
                    }
                }

            }

            return md.digest();
        }
    }

    public static byte[] encode(String origin) {
        if(origin != null && origin.length() != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] bytes = md.digest(origin.getBytes());
                return bytes;
            }
        } else {
            return null;
        }
    }

    public static byte[] encode16(String origin) {
        if(origin != null && origin.length() != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] bytes = md.digest(origin.getBytes());
                byte[] dstBytes = new byte[8];
                System.arraycopy(bytes, 4, dstBytes, 0, 8);
                Object bytes1 = null;
                return dstBytes;
            }
        } else {
            return null;
        }
    }

    public static byte[] encode(String origin, String enc) throws UnsupportedEncodingException {
        if(origin != null && origin.length() != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] bytes = md.digest(origin.getBytes(enc));
                return bytes;
            }
        } else {
            return null;
        }
    }

    public static byte[] encode16(String origin, String enc) throws UnsupportedEncodingException {
        if(origin != null && origin.length() != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] bytes = md.digest(origin.getBytes(enc));
                byte[] dstBytes = new byte[8];
                System.arraycopy(bytes, 4, dstBytes, 0, 8);
                Object bytes1 = null;
                return dstBytes;
            }
        } else {
            return null;
        }
    }

    public static byte[] encode(byte[] bytes) {
        if(bytes != null && bytes.length != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] resultbytes = md.digest(bytes);
                return resultbytes;
            }
        } else {
            return null;
        }
    }

    public static byte[] encode16(byte[] bytes) {
        if(bytes != null && bytes.length != 0) {
            MessageDigest md = getMessageDigest();
            if(md == null) {
                throw new IllegalAccessError("no md5 algorithm");
            } else {
                byte[] resultbytes = md.digest(bytes);
                byte[] dstBytes = new byte[8];
                System.arraycopy(resultbytes, 4, dstBytes, 0, 8);
                Object bytes1 = null;
                return dstBytes;
            }
        } else {
            return null;
        }
    }
}
