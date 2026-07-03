package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class UserProfile {
    public final int id;
    public final String username;
    public final String nickname;
    public final String bio;
    public final String createdAt;
    public final int postCount;
    public final int commentCount;
    public final int likeCount;
    public final List<RecentPost> recentLikedPosts;
    public final List<RecentComment> recentComments;

    private UserProfile(
            int id,
            String username,
            String nickname,
            String bio,
            String createdAt,
            int postCount,
            int commentCount,
            int likeCount,
            List<RecentPost> recentLikedPosts,
            List<RecentComment> recentComments
    ) {
        this.id = id;
        this.username = clean(username);
        this.nickname = clean(nickname);
        this.bio = clean(bio);
        this.createdAt = clean(createdAt);
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.recentLikedPosts = recentLikedPosts;
        this.recentComments = recentComments;
    }

    static UserProfile fromJson(JSONObject root) {
        JSONObject user = root.optJSONObject("user");
        JSONObject stats = root.optJSONObject("stats");
        return new UserProfile(
                user == null ? -1 : user.optInt("id", -1),
                user == null ? "" : user.optString("username", ""),
                user == null ? "" : user.optString("nickname", ""),
                user == null ? "" : user.optString("bio", ""),
                user == null ? "" : user.optString("created_at", ""),
                stats == null ? 0 : stats.optInt("postCount", 0),
                stats == null ? 0 : stats.optInt("commentCount", 0),
                stats == null ? 0 : stats.optInt("likeCount", 0),
                parseRecentPosts(root.optJSONArray("recentLikedPosts")),
                parseRecentComments(root.optJSONArray("recentComments"))
        );
    }

    public String displayName() {
        return !nickname.isEmpty() ? nickname : username;
    }

    private static List<RecentPost> parseRecentPosts(JSONArray items) {
        List<RecentPost> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            result.add(new RecentPost(
                    item.optInt("post_id", -1),
                    item.optString("title", ""),
                    item.optString("category", ""),
                    item.optString("liked_at", item.optString("created_at", ""))
            ));
        }
        return result;
    }

    private static List<RecentComment> parseRecentComments(JSONArray items) {
        List<RecentComment> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            JSONObject post = item.optJSONObject("post");
            result.add(new RecentComment(
                    item.optInt("comment_id", -1),
                    item.optString("content", ""),
                    item.optString("created_at", ""),
                    post == null ? -1 : post.optInt("post_id", -1),
                    post == null ? "" : post.optString("title", "")
            ));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }

    public static final class RecentPost {
        public final int postId;
        public final String title;
        public final String category;
        public final String createdAt;

        RecentPost(int postId, String title, String category, String createdAt) {
            this.postId = postId;
            this.title = clean(title);
            this.category = clean(category);
            this.createdAt = clean(createdAt);
        }
    }

    public static final class RecentComment {
        public final int commentId;
        public final String content;
        public final String createdAt;
        public final int postId;
        public final String postTitle;

        RecentComment(int commentId, String content, String createdAt, int postId, String postTitle) {
            this.commentId = commentId;
            this.content = clean(content);
            this.createdAt = clean(createdAt);
            this.postId = postId;
            this.postTitle = clean(postTitle);
        }
    }
}
