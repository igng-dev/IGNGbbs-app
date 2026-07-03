package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorDailyLimit {
    public final boolean allowed;
    public final int used;
    public final int limit;
    public final int level;
    public final int remaining;

    public CreatorDailyLimit(boolean allowed, int used, int limit, int level, int remaining) {
        this.allowed = allowed;
        this.used = used;
        this.limit = limit;
        this.level = level;
        this.remaining = remaining;
    }

    static CreatorDailyLimit fromJson(JSONObject json) {
        return new CreatorDailyLimit(
                json.optBoolean("allowed", true),
                json.optInt("used", 0),
                json.optInt("limit", 0),
                json.optInt("level", 1),
                json.optInt("remaining", 0)
        );
    }
}
