package com.example.igngbbs;

import org.json.JSONObject;

public final class ColumnItem {
    public final int id;
    public final String name;
    public final String slug;
    public final String description;

    public ColumnItem(int id, String name, String slug, String description) {
        this.id = id;
        this.name = clean(name);
        this.slug = clean(slug);
        this.description = clean(description);
    }

    static ColumnItem fromJson(JSONObject json) {
        return new ColumnItem(
                json.optInt("id", -1),
                json.optString("name", ""),
                json.optString("slug", ""),
                json.optString("description", "")
        );
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
