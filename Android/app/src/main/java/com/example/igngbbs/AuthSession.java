package com.example.igngbbs;

import org.json.JSONObject;

public final class AuthSession {
    public final String sessionToken;
    public final String expiresAt;
    public final String sessionId;
    public final String deviceName;
    public final String createdAt;
    public final AuthUser user;

    public AuthSession(String sessionToken, String expiresAt, String sessionId, String deviceName, String createdAt, AuthUser user) {
        this.sessionToken = sessionToken == null ? "" : sessionToken;
        this.expiresAt = expiresAt == null ? "" : expiresAt;
        this.sessionId = sessionId == null ? "" : sessionId;
        this.deviceName = deviceName == null ? "" : deviceName;
        this.createdAt = createdAt == null ? "" : createdAt;
        this.user = user == null ? new AuthUser(0, "", "", "") : user;
    }

    static AuthSession fromLoginJson(JSONObject root) {
        return new AuthSession(
                root.optString("sessionToken", ""),
                root.optString("expiresAt", ""),
                root.optString("sessionId", ""),
                root.optString("deviceName", ""),
                root.optString("createdAt", ""),
                AuthUser.fromJson(root.optJSONObject("user"))
        );
    }

    static AuthSession fromMeJson(String token, JSONObject root) {
        return new AuthSession(
                token,
                root.optString("expiresAt", ""),
                root.optString("sessionId", ""),
                root.optString("deviceName", ""),
                root.optString("createdAt", ""),
                AuthUser.fromJson(root.optJSONObject("user"))
        );
    }
}
