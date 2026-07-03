package com.example.igngbbs;

import org.json.JSONObject;

public final class CommentItem {
    public final int id;
    public final int postId;
    public final int parentId;
    public final int userId;
    public final String content;
    public final String authorName;
    public final String createdAt;
    public final int likeCount;
    public final boolean liked;

    private CommentItem(int id, int postId, int parentId, int userId, String content, String authorName, String createdAt, int likeCount, boolean liked) {
        this.id = id;
        this.postId = postId;
        this.parentId = parentId;
        this.userId = userId;
        this.content = content == null ? "" : content;
        this.authorName = authorName == null ? "" : authorName;
        this.createdAt = createdAt == null ? "" : createdAt;
        this.likeCount = likeCount;
        this.liked = liked;
    }

    static CommentItem fromJson(JSONObject json) {
        JSONObject user = json.optJSONObject("user");
        String nickname = user == null ? "" : user.optString("nickname", "");
        String username = user == null ? "" : user.optString("username", "");
        int id = firstPositive(json, "comment_id", "id", "commentId");
        int postId = firstPositive(json, "post_id", "postId");
        int parentId = firstPositive(json, "parent_id", "parentId");
        return new CommentItem(
                id,
                postId,
                parentId,
                user == null ? 0 : user.optInt("id", 0),
                clean(json.optString("content", "")),
                !nickname.isEmpty() ? nickname : username,
                clean(json.optString("created_at", "")),
                json.optInt("like_count", 0),
                json.optBoolean("liked", false)
        );
    }

    public CommentItem withLikeState(LikeState state) {
        return new CommentItem(
                id,
                postId,
                parentId,
                userId,
                content,
                authorName,
                createdAt,
                state.likeCount,
                state.liked
        );
    }

    private static int firstPositive(JSONObject json, String... keys) {
        for (String key : keys) {
            int value = json.optInt(key, 0);
            if (value > 0) return value;
        }
        return 0;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
