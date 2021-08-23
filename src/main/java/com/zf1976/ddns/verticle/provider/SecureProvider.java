package com.zf1976.ddns.verticle.provider;

import com.zf1976.ddns.config.SecureConfig;
import com.zf1976.ddns.util.AesUtil.AesKey;
import com.zf1976.ddns.util.RsaUtil;
import io.vertx.core.Future;

import java.util.Map;

/**
 * @author ant
 * Create by Ant on 2021/8/4 12:59 PM
 */
public interface SecureProvider {

    /**
     * Read security configuration
     *
     * @return {@link Future<SecureConfig>}
     */
    Future<Map<String, String>> readLoginConfig();

    /**
     * Read RSA key
     *
     * @return {@link Future<RsaUtil.RsaKeyPair>}
     */
    Future<RsaUtil.RsaKeyPair> readRsaKeyPair();

    /**
     * read aes key
     *
     * @return {@link Future<AesKey>}
     */
    Future<AesKey> readAesKey();

}
