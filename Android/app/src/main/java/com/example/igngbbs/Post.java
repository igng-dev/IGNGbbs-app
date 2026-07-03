package com.example.igngbbs;

import org.json.JSONObject;

public final class Post {
    public final int id;
    public final int userId;
    public final int categoryId;
    public final int columnId;
    public final int status;
    public final int banStatus;
    public final boolean pendingReview;
    public final String visibility;
    public final String groupFilterMode;
    public final String accessState;
    public final String accessNotice;
    public final boolean requiresWarning;
    public final String title;
    public final String summary;
    public final String content;
    public final String format;
    public final String tags;
    public final int aiUsage;
    public final String authorName;
    public final String categoryName;
    public final String columnName;
    public final String createdAt;
    public final int views;
    public final int likes;
    public final int comments;
    public final java.util.List<SeriesPost> seriesPosts;

    private Post(
            int id,
            int userId,
            int categoryId,
            int columnId,
            int status,
            int banStatus,
            boolean pendingReview,
            String visibility,
            String groupFilterMode,
            String accessState,
            String accessNotice,
            boolean requiresWarning,
            String title,
            String summary,
            String content,
            String format,
            String tags,
            int aiUsage,
            String authorName,
            String categoryName,
            String columnName,
            String createdAt,
            int views,
            int likes,
            int comments,
            java.util.List<SeriesPost> seriesPosts
    ) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.columnId = columnId;
        this.status = status;
        this.banStatus = banStatus;
        this.pendingReview = pendingReview;
        this.visibility = visibility;
        this.groupFilterMode = groupFilterMode;
        this.accessState = accessState;
        this.accessNotice = accessNotice;
        this.requiresWarning = requiresWarning;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.format = format;
        this.tags = tags;
        this.aiUsage = aiUsage;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.columnName = columnName;
        this.createdAt = createdAt;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
        this.seriesPosts = seriesPosts;
    }

    static Post fromJson(JSONObject json) {
        JSONObject user = json.optJSONObject("user");
        JSONObject category = json.optJSONObject("category");
        JSONObject column = json.optJSONObject("column");
        String nickname = user == null ? "" : user.optString("nickname", "");
        String username = user == null ? "" : user.optString("username", "");

        return new Post(
                json.optInt("post_id"),
                json.optInt("user_id", user == null ? -1 : user.optInt("id", -1)),
                json.optInt("category_id", category == null ? -1 : category.optInt("category_id", -1)),
                json.optInt("column_id", column == null ? -1 : column.optInt("id", -1)),
                json.optInt("status", -1),
                json.optInt("ban_status", -1),
                json.optBoolean("pending_review", false),
                clean(json.optString("visibility", "public")),
                clean(json.optString("group_filter_mode", "include")),
                clean(json.optString("access_state", "")),
                clean(json.optString("access_notice", "")),
                json.optBoolean("requires_warning", false),
                clean(json.optString("title", "未命名文章")),
                clean(json.optString("summary", "")),
                clean(json.optString("content", "")),
                clean(json.optString("format", "markdown")),
                clean(json.optString("tags", "")),
                json.optInt("ai_usage", 0),
                !nickname.isEmpty() ? nickname : username,
                category == null ? "未分类" : clean(category.optString("name", "未分类")),
                column == null ? "" : clean(column.optString("name", "")),
                clean(json.optString("created_at", "")),
                json.optInt("views", 0),
                json.optInt("like_count", 0),
                json.optInt("comment_count", 0),
                parseSeriesPosts(json)
        );
    }

    private static java.util.List<SeriesPost> parseSeriesPosts(JSONObject json) {
        java.util.List<SeriesPost> result = new java.util.ArrayList<>();
        org.json.JSONArray items = json.optJSONArray("series_posts");
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject wrapper = items.optJSONObject(i);
            if (wrapper == null) continue;
            JSONObject series = wrapper.optJSONObject("series");
            if (series == null) continue;
            org.json.JSONArray posts = series.optJSONArray("posts");
            if (posts == null) continue;
            for (int j = 0; j < posts.length(); j++) {
                JSONObject seriesPost = posts.optJSONObject(j);
                if (seriesPost == null) continue;
                JSONObject linkedPost = seriesPost.optJSONObject("post");
                if (linkedPost == null) continue;
                result.add(new SeriesPost(
                        series.optInt("id", -1),
                        clean(series.optString("name", "")),
                        linkedPost.optInt("post_id", -1),
                        clean(linkedPost.optString("title", "")),
                        seriesPost.optInt("order", j)
                ));
            }
            break;
        }
        result.sort((a, b) -> Integer.compare(a.order, b.order));
        return result;
    }

    public boolean isPublicListVisible() {
        return status == 0
                && banStatus == 0
                && ("public".equals(visibility) || visibility.isEmpty());
    }

    public boolean shouldAppearInAppList() {
        return status == 0 && banStatus == 0;
    }

    public boolean hasVisibilityBadge() {
        return "group".equals(visibility) || "private".equals(visibility);
    }

    public String visibilityBadgeText() {
        if ("group".equals(visibility)) return "群组可见";
        if ("private".equals(visibility)) return "私密";
        return "";
    }

    public boolean hasAccessNotice() {
        return !accessNotice.trim().isEmpty()
                || requiresWarning
                || "restricted".equals(accessState)
                || "group".equals(accessState)
                || "private".equals(accessState);
    }

    public String accessNoticeText() {
        if (!accessNotice.trim().isEmpty()) return accessNotice.trim();
        if ("restricted".equals(accessState)) return "这篇文章已被标记为限制查看。";
        if ("group".equals(accessState)) return "这是群组可见文章。";
        if ("private".equals(accessState)) return "这是私密文章。";
        return "";
    }

    public String safeSummary() {
        if (!summary.trim().isEmpty()) {
            return summary.trim();
        }
        String stripped = content.replaceAll("(?s)<[^>]*>", "")
                .replaceAll("[#*_`>\\[\\]()]"," ")
                .replaceAll("\\s+", " ")
                .trim();
        return stripped.length() > 120 ? stripped.substring(0, 120) + "..." : stripped;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }

    public static final class SeriesPost {
        public final int seriesId;
        public final String seriesName;
        public final int postId;
        public final String title;
        public final int order;

        SeriesPost(int seriesId, String seriesName, int postId, String title, int order) {
            this.seriesId = seriesId;
            this.seriesName = seriesName;
            this.postId = postId;
            this.title = title;
            this.order = order;
        }
    }
}
