package com.zf1976.ddns.api.signature.rpc;

import com.zf1976.ddns.api.auth.BasicCredentials;
import com.zf1976.ddns.api.enums.MethodType;
import com.zf1976.ddns.api.signature.Signer;
import com.zf1976.ddns.util.ApiURLEncoder;
import com.zf1976.ddns.util.ParameterHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author mac
 * @date 2021/7/22
 */
public class HuaweiSignatureComposer implements RpcAPISignatureComposer {

    private static RpcAPISignatureComposer composer = null;
    private final Signer signer = Signer.getSHA256Signer();

    private HuaweiSignatureComposer() {

    }

    public static RpcAPISignatureComposer getComposer() {
        if (null == composer) {
            synchronized (AliyunSignatureComposer.class) {
                if (null == composer) {
                    composer = new HuaweiSignatureComposer();
                }
            }
        }
        return composer;
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                sb.append("0");
            } else if (hex.length() == 8) {
                hex = hex.substring(6);
            }

            sb.append(hex);
        }

        return sb.toString()
                 .toLowerCase(Locale.getDefault());
    }

    @Override
    public String composeStringToSign(MethodType methodType, Map<String, Object> queries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String composeStringToSign(HttpRequest request, Map<String, Object> queries) {
        String singerDate = request.headers()
                                   .firstValue("X-Sdk-Date")
                                   .orElseGet(() -> {
                                       String date = ParameterHelper.getISO8601Time2(new Date());
                                       request.headers()
                                              .map()
                                              .computeIfAbsent("X-Sdk-Date", k -> new ArrayList<>(1))
                                              .add(date);
                                       return date;
                                   });
        String contentSha256 = this.calculateContentHash(request);
        String[] signedHeaders = this.getSignedHeaders(request);
        String canonicalRequest = this.createCanonicalRequest(request, queries, signedHeaders, contentSha256);
        return this.createStringToSign(canonicalRequest, singerDate);
    }

    @Override
    public String toSignatureUrl(String accessKeySecret,
                                 String urlPattern,
                                 MethodType methodType,
                                 Map<String, Object> queries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String signatureMethod() {
        return null;
    }

    @Override
    public String getSignerVersion() {
        return null;
    }

    @Override
    public void signRequest(BasicCredentials basicCredentials, HttpRequest request, Map<String, Object> queries) {
        final var stringToSign = this.composeStringToSign(request, queries);
        final var signedHeaders = this.getSignedHeaders(request);
        byte[] signature = this.computeSignature(stringToSign, basicCredentials.getAccessKeySecret());
        final var authorizationHeader = this.buildAuthorizationHeader(signedHeaders, signature, basicCredentials.getAccessKeyId());
        request.headers()
               .map()
               .computeIfAbsent("Authorization", k -> new ArrayList<>(1))
               .add(authorizationHeader);
    }

    private String buildAuthorizationHeader(String[] signedHeaders, byte[] signature, String accessKey) {
        String credential = "Access=" + accessKey;
        String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(signedHeaders);
        String signatureHeader = "Signature=" + toHex(signature);
        return "SDK-HMAC-SHA256" + " " + credential + ", " + signerHeaders + ", " + signatureHeader;
    }

    protected final byte[] computeSignature(String stringToSign, String secret) {
        return this.signer.signString(stringToSign, secret)
                          .getBytes(StandardCharsets.UTF_8);
    }

    protected String createStringToSign(String canonicalRequest, String singerDate) {
        return "SDK-HMAC-SHA256" + "\n" + singerDate + "\n" + toHex(this.hash(canonicalRequest));
    }

    /**
     * 加密sha256消息摘要加密body
     *
     * @param request 请求
     * @return {@link String}
     */
    protected String calculateContentHash(HttpRequest request) {
        return request.headers()
                      .firstValue("x-sdk-content-sha256")
                      .orElseGet(() -> {
                          final var contentSHA256 = request.bodyPublisher()
                                                           .orElseThrow(() -> new RuntimeException("bdoy cannot been null"));
                          return toHex(this.hash(contentSHA256.toString()));
                      });
    }

    protected String[] getSignedHeaders(HttpRequest request) {
        String[] signedHeaders = request.headers()
                                        .map()
                                        .keySet()
                                        .toArray(new String[]{});
        Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
        return signedHeaders;
    }

    protected String createCanonicalRequest(HttpRequest request,
                                            Map<String, Object> queries,
                                            String[] signedHeaders,
                                            String contentSha256) {
        return request.method() + "\n" +
                this.getCanonicalizedResourcePath(this.getPath(request)) +
                "\n" +
                this.getCanonicalizedQueryString(queries) +
                "\n" +
                this.getCanonicalizedHeaderString(request, signedHeaders) +
                "\n" +
                this.getSignedHeadersString(signedHeaders) + "\n" +
                contentSha256;
    }

    @SuppressWarnings("rawtypes")
    protected String getCanonicalizedQueryString(Map<String, Object> parameters) {
        SortedMap<String, Object> sorted = new TreeMap<>();

        for (Map.Entry<String, Object> stringListEntry : parameters.entrySet()) {
            String encodedParamName = ApiURLEncoder.urlEncode(stringListEntry.getKey(), false);
            sorted.put(encodedParamName, ApiURLEncoder.urlEncode(stringListEntry.getValue()
                                                                                .toString(), false));
        }

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Object> stringListEntry : sorted.entrySet()) {

            String value;
            for (Iterator var13 = ((List) stringListEntry.getValue()).iterator(); var13.hasNext(); result.append(stringListEntry.getKey())
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

    protected String getCanonicalizedHeaderString(HttpRequest request, String[] signedHeaders) {
        final var listMap = request.headers()
                                   .map();
        StringBuilder buffer = new StringBuilder();

        for (String header : signedHeaders) {
            String key = header.toLowerCase();
            String value = listMap.get(header)
                                  .get(0);
            buffer.append(key)
                  .append(":");
            if (value != null) {
                buffer.append(value.trim());
            }

            buffer.append("\n");
        }

        return buffer.toString();
    }

    protected String getCanonicalizedResourcePath(String resourcePath) {
        if (resourcePath != null && !resourcePath.isEmpty()) {
            try {
                resourcePath = (new URI(resourcePath)).getPath();
            } catch (URISyntaxException var3) {
                return resourcePath;
            }

            String value = ApiURLEncoder.urlEncode(resourcePath, true);
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

    public String getPath(HttpRequest request) {
        String url = request.uri()
                            .getPath();
        int i = url.indexOf("://");
        if (i >= 0) {
            url = url.substring(i + 3);
        }

        i = url.indexOf(47);
        return i >= 0 ? url.substring(i) : "/";
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
