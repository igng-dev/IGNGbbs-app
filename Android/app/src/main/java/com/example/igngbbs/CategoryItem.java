package com.example.igngbbs;

import org.json.JSONObject;

public final class CategoryItem {
    public final int id;
    public final String name;
    public final String slug;

    public CategoryItem(int id, String name, String slug) {
        this.id = id;
        this.name = clean(name);
        this.slug = clean(slug);
    }

    static CategoryItem fromJson(JSONObject json) {
        return new CategoryItem(
                json.optInt("category_id", json.optInt("id", 0)),
                json.optString("name", ""),
                json.optString("slug", "")
        );
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
