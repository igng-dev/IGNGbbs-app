package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorPersonalization {
    public final String excludedCats;
    public final String excludedTags;
    public final String excludedUsers;
    public final boolean hideAi;
    public final List<CategoryOption> categories;
    public final List<UserOption> excludedUserDetails;

    public CreatorPersonalization(String excludedCats, String excludedTags, String excludedUsers, boolean hideAi, List<CategoryOption> categories, List<UserOption> excludedUserDetails) {
        this.excludedCats = clean(excludedCats);
        this.excludedTags = clean(excludedTags);
        this.excludedUsers = clean(excludedUsers);
        this.hideAi = hideAi;
        this.categories = categories;
        this.excludedUserDetails = excludedUserDetails;
    }

    static CreatorPersonalization fromJson(JSONObject root) {
        JSONObject preference = root.optJSONObject("preference");
        if (preference == null) preference = new JSONObject();
        return new CreatorPersonalization(
                preference.optString("excluded_cats", ""),
                preference.optString("excluded_tags", ""),
                preference.optString("excluded_users", ""),
                preference.optBoolean("hide_ai", false),
                CategoryOption.listFromJson(root.optJSONArray("categories")),
                UserOption.listFromJson(root.optJSONArray("excluded_user_details"))
        );
    }

    public static final class UserOption {
        public final int id;
        public final String username;
        public final String nickname;

        UserOption(int id, String username, String nickname) {
            this.id = id;
            this.username = clean(username);
            this.nickname = clean(nickname);
        }

        static List<UserOption> listFromJson(JSONArray items) {
            List<UserOption> result = new ArrayList<>();
            if (items == null) return result;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item == null) continue;
                result.add(new UserOption(
                        item.optInt("id", 0),
                        item.optString("username", ""),
                        item.optString("nickname", "")
                ));
            }
            return result;
        }

        public String displayName() {
            return nickname.isEmpty() ? username : nickname;
        }
    }

    public static final class CategoryOption {
        public final int id;
        public final String name;
        public final String slug;

        CategoryOption(int id, String name, String slug) {
            this.id = id;
            this.name = clean(name);
            this.slug = clean(slug);
        }

        static List<CategoryOption> listFromJson(JSONArray items) {
            List<CategoryOption> result = new ArrayList<>();
            if (items == null) return result;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item == null) continue;
                result.add(new CategoryOption(
                        item.optInt("category_id", 0),
                        item.optString("name", ""),
                        item.optString("slug", "")
                ));
            }
            return result;
        }
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
