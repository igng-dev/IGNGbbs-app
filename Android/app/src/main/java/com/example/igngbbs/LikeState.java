package com.example.igngbbs;

import org.json.JSONObject;

public final class LikeState {
    public final boolean liked;
    public final int likeCount;

    public LikeState(boolean liked, int likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    static LikeState fromJson(JSONObject json) {
        return new LikeState(
                json.optBoolean("liked", false),
                json.optInt("like_count", 0)
        );
    }
}
