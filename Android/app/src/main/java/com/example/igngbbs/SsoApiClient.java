package com.example.igngbbs;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SsoApiClient {
    private final String baseUrl;

    public SsoApiClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public MathCaptcha fetchMathCaptcha() throws Exception {
        return MathCaptcha.fromJson(requestJson("GET", "/api/auth/math-captcha", null, null));
    }

    public AuthSession login(
            String identifier,
            String password,
            String duration,
            String deviceName,
            String mathCaptchaToken,
            String mathCaptchaAnswer
    ) throws Exception {
        JSONObject body = new JSONObject()
                .put("identifier", identifier)
                .put("password", password)
                .put("duration", duration)
                .put("deviceName", deviceName)
                .put("mathCaptchaToken", mathCaptchaToken)
                .put("mathCaptchaAnswer", mathCaptchaAnswer);
        return AuthSession.fromLoginJson(requestJson("POST", "/api/mobile/auth/login", body, null));
    }

    public AuthSession me(String token) throws Exception {
        return AuthSession.fromMeJson(token, requestJson("GET", "/api/mobile/auth/me", null, token));
    }

    public void logout(String token) throws Exception {
        requestJson("POST", "/api/mobile/auth/logout", new JSONObject(), token);
    }

    private JSONObject requestJson(String method, String path, JSONObject body, String token) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", "IGNGbbs-Android/1.0");
            if (token != null && !token.trim().isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token.trim());
            }
            if (body != null && !"GET".equals(method)) {
                conn.setDoOutput(true);
                try (OutputStream stream = conn.getOutputStream()) {
                    stream.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            String response = readFully(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            JSONObject json = response.isEmpty() ? new JSONObject() : new JSONObject(response);
            if (code >= 400) {
                throw new IOException(json.optString("error", "请求失败：" + code));
            }
            return json;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
