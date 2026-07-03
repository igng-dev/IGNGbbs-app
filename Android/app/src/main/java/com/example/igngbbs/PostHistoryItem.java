package com.example.igngbbs;

import org.json.JSONObject;

public final class PostHistoryItem {
    public final int id;
    public final String visitedAt;
    public final String progressAnchor;
    public final String deviceName;
    public final Post post;

    private PostHistoryItem(
            int id,
            String visitedAt,
            String progressAnchor,
            String deviceName,
            Post post
    ) {
        this.id = id;
        this.visitedAt = visitedAt;
        this.progressAnchor = progressAnchor;
        this.deviceName = deviceName;
        this.post = post;
    }

    static PostHistoryItem fromJson(JSONObject json) {
        JSONObject postJson = json.optJSONObject("post");
        return new PostHistoryItem(
                json.optInt("id", 0),
                clean(json.optString("visited_at", "")),
                clean(json.optString("progress_anchor", "")),
                clean(json.optString("device_name", "")),
                postJson == null ? null : Post.fromJson(postJson)
        );
    }

    public boolean hasProgressAnchor() {
        return progressAnchor != null && !progressAnchor.trim().isEmpty();
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
