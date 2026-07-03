package com.example.igngbbs;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorColumn {
    public final int id;
    public final String name;
    public final String slug;
    public final String description;
    public final String createdAt;
    public final String updatedAt;
    public final int creatorId;
    public final String creatorName;
    public final int postCount;
    public final int childCount;
    public final boolean visitorPostAllowed;
    public final String visitorViewScope;
    public final String visitorEditScope;
    public final boolean managerPostAllowed;
    public final String managerViewScope;
    public final String managerEditScope;
    public final int managerCount;
    public final List<String> managerNames;

    public CreatorColumn(
            int id,
            String name,
            String slug,
            String description,
            String createdAt,
            String updatedAt,
            int creatorId,
            String creatorName,
            int postCount,
            int childCount,
            boolean visitorPostAllowed,
            String visitorViewScope,
            String visitorEditScope,
            boolean managerPostAllowed,
            String managerViewScope,
            String managerEditScope,
            int managerCount,
            List<String> managerNames
    ) {
        this.id = id;
        this.name = clean(name);
        this.slug = clean(slug);
        this.description = clean(description);
        this.createdAt = clean(createdAt);
        this.updatedAt = clean(updatedAt);
        this.creatorId = creatorId;
        this.creatorName = clean(creatorName);
        this.postCount = postCount;
        this.childCount = childCount;
        this.visitorPostAllowed = visitorPostAllowed;
        this.visitorViewScope = clean(visitorViewScope);
        this.visitorEditScope = clean(visitorEditScope);
        this.managerPostAllowed = managerPostAllowed;
        this.managerViewScope = clean(managerViewScope);
        this.managerEditScope = clean(managerEditScope);
        this.managerCount = managerCount;
        this.managerNames = managerNames;
    }

    static CreatorColumn fromJson(JSONObject root) {
        JSONObject column = root.optJSONObject("column");
        if (column == null) column = root;
        JSONObject creator = column.optJSONObject("creator");
        JSONObject count = column.optJSONObject("_count");
        org.json.JSONArray managers = column.optJSONArray("managers");
        String nickname = creator == null ? "" : creator.optString("nickname", "");
        String username = creator == null ? "" : creator.optString("username", "");
        return new CreatorColumn(
                column.optInt("id", 0),
                column.optString("name", ""),
                column.optString("slug", ""),
                column.optString("description", ""),
                column.optString("created_at", ""),
                column.optString("updated_at", ""),
                creator == null ? 0 : creator.optInt("id", 0),
                !nickname.isEmpty() ? nickname : username,
                count == null ? 0 : count.optInt("posts", 0),
                count == null ? 0 : count.optInt("children", 0),
                column.optBoolean("visitor_post_allowed", false),
                column.optString("visitor_view_scope", ""),
                column.optString("visitor_edit_scope", ""),
                column.optBoolean("manager_post_allowed", false),
                column.optString("manager_view_scope", ""),
                column.optString("manager_edit_scope", ""),
                managers == null ? 0 : managers.length(),
                parseManagerNames(managers)
        );
    }

    private static List<String> parseManagerNames(org.json.JSONArray items) {
        List<String> names = new ArrayList<>();
        if (items == null) return names;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            String nickname = clean(item.optString("nickname", ""));
            String username = clean(item.optString("username", ""));
            String displayName = !nickname.isEmpty() ? nickname : username;
            if (!displayName.isEmpty()) {
                names.add(displayName);
            }
        }
        return names;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
