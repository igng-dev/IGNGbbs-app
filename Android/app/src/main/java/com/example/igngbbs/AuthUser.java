package com.example.igngbbs;

import org.json.JSONObject;

public final class AuthUser {
    public final int id;
    public final String username;
    public final String nickname;
    public final String email;

    public AuthUser(int id, String username, String nickname, String email) {
        this.id = id;
        this.username = username == null ? "" : username;
        this.nickname = nickname == null ? "" : nickname;
        this.email = email == null ? "" : email;
    }

    static AuthUser fromJson(JSONObject json) {
        if (json == null) {
            return new AuthUser(0, "", "", "");
        }
        return new AuthUser(
                json.optInt("id"),
                json.optString("username", ""),
                json.optString("nickname", ""),
                json.optString("email", "")
        );
    }

    public String displayName() {
        return nickname.isEmpty() ? username : nickname;
    }
}
