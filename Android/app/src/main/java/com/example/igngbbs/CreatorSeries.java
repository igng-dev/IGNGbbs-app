package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorSeries {
    public final int id;
    public final String name;
    public final String slug;
    public final String description;
    public final String createdAt;
    public final String updatedAt;
    public final int views;
    public final int postCount;
    public final List<SeriesPostItem> posts;

    public CreatorSeries(
            int id,
            String name,
            String slug,
            String description,
            String createdAt,
            String updatedAt,
            int views,
            int postCount,
            List<SeriesPostItem> posts
    ) {
        this.id = id;
        this.name = clean(name);
        this.slug = clean(slug);
        this.description = clean(description);
        this.createdAt = clean(createdAt);
        this.updatedAt = clean(updatedAt);
        this.views = views;
        this.postCount = postCount;
        this.posts = posts;
    }

    static CreatorSeries fromJson(JSONObject root) {
        JSONObject series = root.optJSONObject("series");
        if (series == null) series = root;
        JSONObject count = series.optJSONObject("_count");
        return new CreatorSeries(
                series.optInt("id", 0),
                series.optString("name", ""),
                series.optString("slug", ""),
                series.optString("description", ""),
                series.optString("created_at", ""),
                series.optString("updated_at", ""),
                series.optInt("views", 0),
                count == null ? series.optInt("postCount", 0) : count.optInt("posts", 0),
                parsePosts(series.optJSONArray("posts"))
        );
    }

    private static List<SeriesPostItem> parsePosts(JSONArray items) {
        List<SeriesPostItem> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            JSONObject post = item.optJSONObject("post");
            result.add(new SeriesPostItem(
                    item.optInt("id", 0),
                    item.optInt("order", i),
                    post == null ? 0 : post.optInt("post_id", 0),
                    post == null ? "" : post.optString("title", ""),
                    post == null ? 0 : post.optInt("status", 0),
                    post == null ? 0 : post.optInt("ban_status", 0)
            ));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }

    public static final class SeriesPostItem {
        public final int id;
        public final int order;
        public final int postId;
        public final String title;
        public final int status;
        public final int banStatus;

        SeriesPostItem(int id, int order, int postId, String title, int status, int banStatus) {
            this.id = id;
            this.order = order;
            this.postId = postId;
            this.title = clean(title);
            this.status = status;
            this.banStatus = banStatus;
        }
    }
}
