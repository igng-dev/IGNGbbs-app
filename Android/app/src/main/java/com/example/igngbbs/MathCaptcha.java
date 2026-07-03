package com.example.igngbbs;

import org.json.JSONObject;

public final class MathCaptcha {
    public final int a;
    public final int b;
    public final String token;

    public MathCaptcha(int a, int b, String token) {
        this.a = a;
        this.b = b;
        this.token = token == null ? "" : token;
    }

    static MathCaptcha fromJson(JSONObject root) {
        return new MathCaptcha(
                root.optInt("a"),
                root.optInt("b"),
                root.optString("token", "")
        );
    }

    public String question() {
        return a + " + " + b + " = ?";
    }
}
