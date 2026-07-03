package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public final class CreatorPermissionsState {
    public final String username;
    public final String nickname;
    public final boolean isAdmin;
    public final List<CreatorPermissionItem> items;

    public CreatorPermissionsState(String username, String nickname, boolean isAdmin, List<CreatorPermissionItem> items) {
        this.username = clean(username);
        this.nickname = clean(nickname);
        this.isAdmin = isAdmin;
        this.items = items;
    }

    static CreatorPermissionsState fromJson(JSONObject root) {
        JSONObject user = root.optJSONObject("user");
        JSONArray items = root.optJSONArray("items");
        return new CreatorPermissionsState(
                user == null ? "" : user.optString("username", ""),
                user == null ? "" : user.optString("nickname", ""),
                root.optBoolean("isAdmin", false),
                CreatorPermissionItem.listFromJson(items)
        );
    }

    public String displayName() {
        return nickname.isEmpty() ? username : nickname;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
