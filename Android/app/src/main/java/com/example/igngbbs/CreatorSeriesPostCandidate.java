package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorSeriesPostCandidate {
    public final int postId;
    public final String title;
    public final int status;
    public final int banStatus;
    public final String tags;
    public final String createdAt;
    public final boolean linked;

    public CreatorSeriesPostCandidate(int postId, String title, int status, int banStatus, String tags, String createdAt, boolean linked) {
        this.postId = postId;
        this.title = clean(title);
        this.status = status;
        this.banStatus = banStatus;
        this.tags = clean(tags);
        this.createdAt = clean(createdAt);
        this.linked = linked;
    }

    static CreatorSeriesPostCandidate fromJson(JSONObject json) {
        return new CreatorSeriesPostCandidate(
                json.optInt("post_id", 0),
                json.optString("title", ""),
                json.optInt("status", 0),
                json.optInt("ban_status", 0),
                json.optString("tags", ""),
                json.optString("created_at", ""),
                json.optBoolean("linked", false)
        );
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
