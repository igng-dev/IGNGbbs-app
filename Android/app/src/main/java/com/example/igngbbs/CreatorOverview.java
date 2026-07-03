package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorOverview {
    public final int postCount;
    public final int draftCount;
    public final int privateCount;
    public final int receivedCommentCount;
    public final int todayViews;

    public CreatorOverview(int postCount, int draftCount, int privateCount, int receivedCommentCount, int todayViews) {
        this.postCount = postCount;
        this.draftCount = draftCount;
        this.privateCount = privateCount;
        this.receivedCommentCount = receivedCommentCount;
        this.todayViews = todayViews;
    }

    static CreatorOverview fromJson(JSONObject root) {
        JSONObject stats = root.optJSONObject("stats");
        if (stats == null) {
            stats = root;
        }
        return new CreatorOverview(
                stats.optInt("postCount", 0),
                stats.optInt("draftCount", 0),
                stats.optInt("privateCount", 0),
                stats.optInt("receivedCommentCount", 0),
                stats.optInt("todayViews", 0)
        );
    }
}
