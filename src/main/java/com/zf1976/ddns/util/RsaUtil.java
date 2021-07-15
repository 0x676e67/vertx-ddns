package com.zf1976.ddns.util;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author mac
 * Create by Ant on 2020/9/2 下午8:05
 */
public class RsaUtil {

    public static final String CIPHER = "RSA";
    public static final Base64.Decoder BASE_64_DECODER = Base64.getDecoder();
    public static final Base64.Encoder BASE_64_ENCODER = Base64.getEncoder();

    /**
     * 公钥解密
     *
     * @param key 公钥
     * @param content          待解密的信息
     * @return /
     * @throws Exception /
     */
    public static String decryptByPublicKey(String key, String content) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(BASE_64_DECODER.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(BASE_64_DECODER.decode(content));
        return new String(result);
    }

    /**
     * 私钥加密
     *
     * @param key 私钥
     * @param content 待加密的信息
     * @return /
     * @throws Exception /
     */
    public static String encryptByPrivateKey(String key, String content) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(BASE_64_DECODER.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return encrypt(cipher, privateKey, content);
    }

    /**
     * 私钥解密
     *
     * @param key 私钥
     * @param content 待解密的文本
     * @return /
     * @throws Exception /
     */
    public static String decryptByPrivateKey(String key, String content) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(BASE_64_DECODER.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(BASE_64_DECODER.decode(content));
        return new String(result);
    }

    /**
     * 公钥加密
     *
     * @param key 公钥
     * @param content 待加密的文本
     * @return /
     */
    public static String encryptByPublicKey(String key, String content) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(BASE_64_DECODER.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance(CIPHER);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance(CIPHER);
        return encrypt(cipher, publicKey, content);
    }

    private static String encrypt(Cipher cipher, Key key, String content) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] inputArray = content.getBytes();
        int inputLength = inputArray.length;
        // 最大加密字节数，超出最大字节数需要分组加密
        int max = 53;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache;
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > max) {
                cache = cipher.doFinal(inputArray, offSet, max);
                offSet += max;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        return BASE_64_ENCODER.encodeToString(resultBytes);
    }

    /**
     * 构建RSA密钥对
     *
     * @return /
     * @throws NoSuchAlgorithmException /
     */
    public static RsaKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CIPHER);
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String publicKeyString = BASE_64_ENCODER.encodeToString(rsaPublicKey.getEncoded());
        String privateKeyString = BASE_64_ENCODER.encodeToString(rsaPrivateKey.getEncoded());
        return new RsaKeyPair(publicKeyString, privateKeyString);
    }


    /**
     * RSA密钥对对象
     */
    public static class RsaKeyPair {

        private String publicKey;
        private String privateKey;

        public RsaKeyPair() {
        }

        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        @Override
        public String toString() {
            return "RsaKeyPair{" +
                    "publicKey='" + publicKey + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    '}';
        }
    }
}
