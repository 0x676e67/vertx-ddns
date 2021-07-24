package com.zf1976.ddns.api.signature;

import com.zf1976.ddns.util.ApiURLEncoder;
import com.zf1976.ddns.util.BinaryUtils;

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
public class HuaweiSigner {
    public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    public static final String HOST = "Host";
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("SDK-HMAC-SHA256\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");

    static {
        TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public HuaweiSigner() {
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

        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            String encodedParamName = ApiURLEncoder.huaweiPercentEncode(entry.getKey(), false);
            List<String> paramValues = entry.getValue();
            List<String> encodedValues = new ArrayList<>(paramValues.size());

            for (String value : paramValues) {
                encodedValues.add(ApiURLEncoder.huaweiPercentEncode(value, false));
            }

            Collections.sort(encodedValues);
            sorted.put(encodedParamName, encodedValues);
        }

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, List<String>> stringListEntry : sorted.entrySet()) {

            String value;
            for (var var13 = (stringListEntry.getValue()).iterator(); var13.hasNext(); result.append(stringListEntry.getKey())
                                                                                             .append("=")
                                                                                             .append(value)) {
                value = var13.next();
                if (result.length() > 0) {
                    result.append("&");
                }
            }
        }

        return result.toString();
    }

    protected String createCanonicalRequest(Request request, String[] signedHeaders, String contentSha256) {
        return request.getMethod()
                      .toString() + "\n" +
                this.getCanonicalizedResourcePath(request.getPath()) +
                "\n" +
                this.getCanonicalizedQueryString(request.getQueryStringParams()) +
                "\n" +
                this.getCanonicalizedHeaderString(request, signedHeaders) +
                "\n" +
                this.getSignedHeadersString(signedHeaders) +
                "\n" +
                contentSha256;
    }

    protected String createStringToSign(String canonicalRequest, String singerDate) {
        return "SDK-HMAC-SHA256" +
                "\n" +
                singerDate +
                "\n" +
                BinaryUtils.toHex(this.hash(canonicalRequest));
    }

    private byte[] deriveSigningKey(String secret) {
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
        return "SDK-HMAC-SHA256" +
                " " +
                credential +
                ", " +
                signerHeaders +
                ", " +
                signatureHeader;
    }

    protected String[] getSignedHeaders(Request request) {
        String[] signedHeaders = request.getHeaders()
                                        .keySet()
                                        .toArray(new String[0]);
        Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
        return signedHeaders;
    }

    protected String getCanonicalizedHeaderString(Request request, String[] signedHeaders) {
        Map<String, String> requestHeaders = request.getHeaders();
        StringBuilder buffer = new StringBuilder();

        for (String header : signedHeaders) {
            String key = header.toLowerCase();
            String value = requestHeaders.get(header);
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

        for (String header : signedHeaders) {
            if (buffer.length() > 0) {
                buffer.append(";");
            }

            buffer.append(header.toLowerCase());
        }

        return buffer.toString();
    }

    protected void addHostHeader(Request request) {
        boolean haveHostHeader = false;

        for (String key : request.getHeaders()
                                 .keySet()) {
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
            var var4 = headers.keySet()
                              .iterator();

            String key;
            do {
                if (!var4.hasNext()) {
                    return null;
                }

                key = var4.next();
            } while (!header.equalsIgnoreCase(key));

            return headers.get(key);
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
