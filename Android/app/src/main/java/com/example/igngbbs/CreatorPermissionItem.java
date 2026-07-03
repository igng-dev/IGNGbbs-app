package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorPermissionItem {
    public final String key;
    public final String label;
    public final String description;
    public final boolean enabled;

    public CreatorPermissionItem(String key, String label, String description, boolean enabled) {
        this.key = clean(key);
        this.label = clean(label);
        this.description = clean(description);
        this.enabled = enabled;
    }

    static List<CreatorPermissionItem> listFromJson(JSONArray items) {
        List<CreatorPermissionItem> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            result.add(new CreatorPermissionItem(
                    item.optString("key", ""),
                    item.optString("label", ""),
                    item.optString("desc", ""),
                    item.optBoolean("enabled", false)
            ));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
