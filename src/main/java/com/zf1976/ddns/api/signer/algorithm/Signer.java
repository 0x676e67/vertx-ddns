/*
 *
 *
 * MIT License
 *
 * Copyright (c) 2021 zf1976
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zf1976.ddns.api.signer.algorithm;

import com.zf1976.ddns.api.auth.ProviderCredentials;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Signer {

    private final static Signer hmacSHA1Signer = new HmacSHA1Signer();
    private final static Signer hmacSHA256Signer = new HmacSHA256Signer();

    public static Signer getSHA1Signer() {
        return hmacSHA1Signer;
    }

    public static Signer getSHA256Signer() {
        return hmacSHA256Signer;
    }

    protected static byte[] sign(String stringToSign, String secret, String algorithmName) {
        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithmName));
            return mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException var5) {
            throw new IllegalArgumentException(var5.toString());
        }
    }

    public abstract byte[] signString(String stringToSign, ProviderCredentials credentials);

    public abstract String getSignerName();

    public abstract String getSignerVersion();

    public abstract String getSignerType();

    public abstract byte[] signString(String stringToSign, String secret);
}
