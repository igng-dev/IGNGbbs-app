package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorComment {
    public final int id;
    public final int userId;
    public final int postId;
    public final int parentId;
    public final String content;
    public final String createdAt;
    public final String authorName;
    public final String postTitle;
    public final int mentionId;
    public final boolean mentionUnread;

    public CreatorComment(int id, int userId, int postId, int parentId, String content, String createdAt, String authorName, String postTitle, int mentionId, boolean mentionUnread) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.parentId = parentId;
        this.content = clean(content);
        this.createdAt = clean(createdAt);
        this.authorName = clean(authorName);
        this.postTitle = clean(postTitle);
        this.mentionId = mentionId;
        this.mentionUnread = mentionUnread;
    }

    static CreatorComment fromJson(JSONObject json) {
        JSONObject user = json.optJSONObject("user");
        JSONObject post = json.optJSONObject("post");
        JSONObject mention = json.optJSONArray("mentions") == null ? null : json.optJSONArray("mentions").optJSONObject(0);
        String nickname = user == null ? "" : user.optString("nickname", "");
        String username = user == null ? "" : user.optString("username", "");
        return new CreatorComment(
                json.optInt("comment_id", json.optInt("id", 0)),
                user == null ? 0 : user.optInt("id", 0),
                post == null ? json.optInt("post_id", 0) : post.optInt("post_id", 0),
                json.optJSONObject("parent") == null ? json.optInt("parent_id", 0) : json.optJSONObject("parent").optInt("comment_id", 0),
                json.optString("content", ""),
                json.optString("created_at", ""),
                !nickname.isEmpty() ? nickname : username,
                post == null ? "" : post.optString("title", ""),
                mention == null ? 0 : mention.optInt("id", 0),
                mention != null && mention.optInt("is_read", 1) == 0
        );
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
