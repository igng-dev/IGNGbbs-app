package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorGroup {
    public final int id;
    public final String name;
    public final String createdAt;
    public final List<GroupMember> members;

    public CreatorGroup(int id, String name, String createdAt, List<GroupMember> members) {
        this.id = id;
        this.name = clean(name);
        this.createdAt = clean(createdAt);
        this.members = members;
    }

    static List<CreatorGroup> listFromJson(JSONArray items) {
        List<CreatorGroup> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            result.add(new CreatorGroup(
                    item.optInt("id", 0),
                    item.optString("name", ""),
                    item.optString("created_at", ""),
                    GroupMember.listFromJson(item.optJSONArray("members"))
            ));
        }
        return result;
    }

    public static final class GroupMember {
        public final int id;
        public final String username;
        public final String nickname;

        GroupMember(int id, String username, String nickname) {
            this.id = id;
            this.username = clean(username);
            this.nickname = clean(nickname);
        }

        static List<GroupMember> listFromJson(JSONArray items) {
            List<GroupMember> result = new ArrayList<>();
            if (items == null) return result;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item == null) continue;
                result.add(new GroupMember(
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

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
