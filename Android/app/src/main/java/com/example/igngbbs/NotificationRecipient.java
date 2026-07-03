package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class NotificationRecipient {
    public final int id;
    public final int notificationId;
    public final String title;
    public final String content;
    public final String createdAt;
    public final String creatorName;
    public final String audienceType;
    public final String audienceLabel;
    public final String audienceUsersLabel;
    public final int isRead;
    public final String readAt;

    public NotificationRecipient(
            int id,
            int notificationId,
            String title,
            String content,
            String createdAt,
            String creatorName,
            String audienceType,
            String audienceLabel,
            String audienceUsersLabel,
            int isRead,
            String readAt
    ) {
        this.id = id;
        this.notificationId = notificationId;
        this.title = clean(title);
        this.content = clean(content);
        this.createdAt = clean(createdAt);
        this.creatorName = clean(creatorName);
        this.audienceType = clean(audienceType);
        this.audienceLabel = clean(audienceLabel);
        this.audienceUsersLabel = clean(audienceUsersLabel);
        this.isRead = isRead;
        this.readAt = clean(readAt);
    }

    static NotificationRecipient fromJson(JSONObject json) {
        JSONObject notification = json.optJSONObject("notification");
        JSONObject creator = notification == null ? null : notification.optJSONObject("creator");
        String nickname = creator == null ? "" : creator.optString("nickname", "");
        String audienceUsersLabel = buildAudienceUsersLabel(notification == null ? null : notification.optJSONArray("audienceUsers"));
        return new NotificationRecipient(
                json.optInt("id", 0),
                json.optInt("notification_id", notification == null ? 0 : notification.optInt("id", 0)),
                notification == null ? "" : notification.optString("title", ""),
                notification == null ? "" : notification.optString("content", ""),
                notification == null ? "" : notification.optString("created_at", ""),
                !nickname.isEmpty() ? nickname : "系统管理员",
                notification == null ? "" : notification.optString("audienceType", ""),
                notification == null ? "" : notification.optString("audienceLabel", ""),
                audienceUsersLabel,
                json.optInt("is_read", 0),
                json.optString("read_at", "")
        );
    }

    public boolean isUnread() {
        return isRead == 0;
    }

    private static String buildAudienceUsersLabel(JSONArray items) {
        if (items == null || items.length() == 0) return "";
        List<String> users = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject user = items.optJSONObject(i);
            if (user == null) continue;
            String nickname = clean(user.optString("nickname", ""));
            String username = clean(user.optString("username", ""));
            if (!nickname.isEmpty() && !username.isEmpty()) {
                users.add(nickname + " (@" + username + ")");
            } else if (!nickname.isEmpty()) {
                users.add(nickname);
            } else if (!username.isEmpty()) {
                users.add("@" + username);
            }
        }
        return String.join("、", users);
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
