package com.zf1976.ddns.api.huawei.signer;

import com.zf1976.ddns.api.huawei.BinaryUtils;
import com.zf1976.ddns.api.huawei.Request;
import com.zf1976.ddns.util.ApiURLEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mac
 * @date 2021/7/24
 */
public class Signer {
    public static final String LINE_SEPARATOR = "\n";
    public static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
    public static final String X_SDK_DATE = "X-Sdk-Date";
    public static final String X_SDK_CONTENT_SHA256 = "x-sdk-content-sha256";
    public static final String AUTHORIZATION = "Authorization";
    public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    public static final String HOST = "Host";
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("SDK-HMAC-SHA256\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
    private static final String LINUX_NEW_LINE = "\n";

    static {
        TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Signer() {
    }

    public void sign(Request request) {
        String singerDate = this.getHeader(request, "X-Sdk-Date");
        if (singerDate == null) {
            singerDate = TIME_FORMATTER.format(new Date());
            request.addHeader("X-Sdk-Date", singerDate);
        }

        this.addHostHeader(request);
        String contentSha256 = this.calculateContentHash(request);
        String[] signedHeaders = this.getSignedHeaders(request);
        String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, contentSha256);
        String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
        byte[] signingKey = this.deriveSigningKey(request.getSecret());
        byte[] signature = this.computeSignature(stringToSign, signingKey);
        request.addHeader("Authorization", this.buildAuthorizationHeader(signedHeaders, signature, request.getKey()));
    }

    protected String getCanonicalizedResourcePath(String resourcePath) {
        if (resourcePath != null && !resourcePath.isEmpty()) {
            try {
                resourcePath = (new URI(resourcePath)).getPath();
            } catch (URISyntaxException var3) {
                return resourcePath;
            }

            String value = ApiURLEncoder.huaweiPercentEncode(resourcePath, true);
            if (!value.startsWith("/")) {
                value = "/".concat(value);
            }

            if (!value.endsWith("/")) {
                value = value.concat("/");
            }

            return value;
        } else {
            return "/";
        }
    }

    protected String getCanonicalizedQueryString(Map<String, List<String>> parameters) {
        SortedMap<String, List<String>> sorted = new TreeMap<>();
        Iterator var3 = parameters.entrySet()
                                  .iterator();

        while (var3.hasNext()) {
            Map.Entry<String, List<String>> entry = (Map.Entry) var3.next();
            String encodedParamName = ApiURLEncoder.huaweiPercentEncode(entry.getKey(), false);
            List<String> paramValues = (List) entry.getValue();
            List<String> encodedValues = new ArrayList<>(paramValues.size());
            Iterator var8 = paramValues.iterator();

            while (var8.hasNext()) {
                String value = (String) var8.next();
                encodedValues.add(ApiURLEncoder.huaweiPercentEncode(value, false));
            }

            Collections.sort(encodedValues);
            sorted.put(encodedParamName, encodedValues);
        }

        StringBuilder result = new StringBuilder();
        Iterator var11 = sorted.entrySet()
                               .iterator();

        while (var11.hasNext()) {
            Map.Entry<String, List<String>> entry = (Map.Entry) var11.next();

            String value;
            for (Iterator var13 = ((List) entry.getValue()).iterator(); var13.hasNext(); result.append((String) entry.getKey())
                                                                                               .append("=")
                                                                                               .append(value)) {
                value = (String) var13.next();
                if (result.length() > 0) {
                    result.append("&");
                }
            }
        }

        return result.toString();
    }

    protected String createCanonicalRequest(Request request, String[] signedHeaders, String contentSha256) {
        StringBuilder canonicalRequestBuilder = new StringBuilder(request.getMethod()
                                                                         .toString());
        canonicalRequestBuilder.append("\n")
                               .append(this.getCanonicalizedResourcePath(request.getPath()))
                               .append("\n")
                               .append(this.getCanonicalizedQueryString(request.getQueryStringParams()))
                               .append("\n")
                               .append(this.getCanonicalizedHeaderString(request, signedHeaders))
                               .append("\n")
                               .append(this.getSignedHeadersString(signedHeaders))
                               .append("\n")
                               .append(contentSha256);
        String canonicalRequest = canonicalRequestBuilder.toString();
        return canonicalRequest;
    }

    protected String createStringToSign(String canonicalRequest, String singerDate) {
        StringBuilder stringToSignBuilder = new StringBuilder("SDK-HMAC-SHA256");
        stringToSignBuilder.append("\n")
                           .append(singerDate)
                           .append("\n")
                           .append(BinaryUtils.toHex(this.hash(canonicalRequest)));
        String stringToSign = stringToSignBuilder.toString();
        return stringToSign;
    }

    private final byte[] deriveSigningKey(String secret) {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    protected byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm.toString());
            mac.init(new SecretKeySpec(key, algorithm.toString()));
            return mac.doFinal(data);
        } catch (InvalidKeyException | NoSuchAlgorithmException var5) {
            return null;
        }
    }

    protected final byte[] computeSignature(String stringToSign, byte[] signingKey) {
        return this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSHA256);
    }

    private String buildAuthorizationHeader(String[] signedHeaders, byte[] signature, String accessKey) {
        String credential = "Access=" + accessKey;
        String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(signedHeaders);
        String signatureHeader = "Signature=" + BinaryUtils.toHex(signature);
        StringBuilder authHeaderBuilder = new StringBuilder();
        authHeaderBuilder.append("SDK-HMAC-SHA256")
                         .append(" ")
                         .append(credential)
                         .append(", ")
                         .append(signerHeaders)
                         .append(", ")
                         .append(signatureHeader);
        return authHeaderBuilder.toString();
    }

    protected String[] getSignedHeaders(Request request) {
        String[] signedHeaders = (String[]) request.getHeaders()
                                                   .keySet()
                                                   .toArray(new String[0]);
        Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
        return signedHeaders;
    }

    protected String getCanonicalizedHeaderString(Request request, String[] signedHeaders) {
        Map<String, String> requestHeaders = request.getHeaders();
        StringBuilder buffer = new StringBuilder();
        String[] var5 = signedHeaders;
        int var6 = signedHeaders.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            String header = var5[var7];
            String key = header.toLowerCase();
            String value = (String) requestHeaders.get(header);
            buffer.append(key)
                  .append(":");
            if (value != null) {
                buffer.append(value.trim());
            }

            buffer.append("\n");
        }

        return buffer.toString();
    }

    protected String getSignedHeadersString(String[] signedHeaders) {
        StringBuilder buffer = new StringBuilder();
        String[] var3 = signedHeaders;
        int var4 = signedHeaders.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String header = var3[var5];
            if (buffer.length() > 0) {
                buffer.append(";");
            }

            buffer.append(header.toLowerCase());
        }

        return buffer.toString();
    }

    protected void addHostHeader(Request request) {
        boolean haveHostHeader = false;
        Iterator var3 = request.getHeaders()
                               .keySet()
                               .iterator();

        while (var3.hasNext()) {
            String key = (String) var3.next();
            if ("Host".equalsIgnoreCase(key)) {
                haveHostHeader = true;
                break;
            }
        }

        if (!haveHostHeader) {
            request.addHeader("Host", request.getHost());
        }

    }

    protected String getHeader(Request request, String header) {
        if (header == null) {
            return null;
        } else {
            Map<String, String> headers = request.getHeaders();
            Iterator var4 = headers.keySet()
                                   .iterator();

            String key;
            do {
                if (!var4.hasNext()) {
                    return null;
                }

                key = (String) var4.next();
            } while (!header.equalsIgnoreCase(key));

            return (String) headers.get(key);
        }
    }

    public boolean verify(Request request) {
        String singerDate = this.getHeader(request, "X-Sdk-Date");
        String authorization = this.getHeader(request, "Authorization");
        Matcher m = AUTHORIZATION_PATTERN.matcher(authorization);
        if (!m.find()) {
            return false;
        } else {
            String[] signedHeaders = m.group(2)
                                      .split(";");
            String contentSha256 = this.calculateContentHash(request);
            String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, contentSha256);
            String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
            byte[] signingKey = this.deriveSigningKey(request.getSecret());
            byte[] signature = this.computeSignature(stringToSign, signingKey);
            String signatureResult = this.buildAuthorizationHeader(signedHeaders, signature, request.getKey());
            return signatureResult.equals(authorization);
        }
    }

    protected String calculateContentHash(Request request) {
        String content_sha256 = this.getHeader(request, "x-sdk-content-sha256");
        return content_sha256 != null ? content_sha256 : BinaryUtils.toHex(this.hash(request.getBody()));
    }

    public byte[] hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException var3) {
            return null;
        }
    }
}
