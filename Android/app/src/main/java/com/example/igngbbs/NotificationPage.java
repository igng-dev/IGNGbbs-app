package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class NotificationPage {
    public final List<NotificationRecipient> items;
    public final int total;

    public NotificationPage(List<NotificationRecipient> items, int total) {
        this.items = items;
        this.total = total;
    }

    static NotificationPage fromJson(JSONObject root) {
        JSONArray notifications = root.optJSONArray("notifications");
        List<NotificationRecipient> items = new ArrayList<>();
        if (notifications != null) {
            for (int i = 0; i < notifications.length(); i++) {
                items.add(NotificationRecipient.fromJson(notifications.optJSONObject(i)));
            }
        }
        return new NotificationPage(items, root.optInt("total", items.size()));
    }
}
