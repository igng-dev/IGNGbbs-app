package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorAnalytics {
    public final Scope scope;
    public final int year;
    public final int month;
    public final Summary summary;
    public final List<Point> data;

    private CreatorAnalytics(Scope scope, int year, int month, Summary summary, List<Point> data) {
        this.scope = scope;
        this.year = year;
        this.month = month;
        this.summary = summary;
        this.data = data;
    }

    static CreatorAnalytics fromJson(JSONObject root) {
        return new CreatorAnalytics(
                Scope.fromJson(root.optJSONObject("scope")),
                root.optInt("year", 0),
                root.optInt("month", 0),
                Summary.fromJson(root.optJSONObject("summary")),
                parsePoints(root.optJSONArray("data"))
        );
    }

    private static List<Point> parsePoints(JSONArray items) {
        List<Point> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            result.add(new Point(
                    clean(item.optString("date", "")),
                    item.optInt("total_views", 0),
                    item.optInt("internal_ref", 0),
                    item.optInt("search_ref", 0),
                    item.optInt("external_ref", 0)
            ));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }

    public static final class Scope {
        public final String type;
        public final Integer id;
        public final String title;

        private Scope(String type, Integer id, String title) {
            this.type = clean(type);
            this.id = id;
            this.title = clean(title);
        }

        static Scope fromJson(JSONObject json) {
            if (json == null) {
                return new Scope("posts", null, "全部文章");
            }
            Integer id = json.isNull("id") ? null : json.optInt("id", 0);
            return new Scope(json.optString("type", "posts"), id, json.optString("title", "全部文章"));
        }
    }

    public static final class Summary {
        public final int total;
        public final int internal;
        public final int search;
        public final int external;

        private Summary(int total, int internal, int search, int external) {
            this.total = total;
            this.internal = internal;
            this.search = search;
            this.external = external;
        }

        static Summary fromJson(JSONObject json) {
            if (json == null) {
                return new Summary(0, 0, 0, 0);
            }
            return new Summary(
                    json.optInt("total", 0),
                    json.optInt("internal", 0),
                    json.optInt("search", 0),
                    json.optInt("external", 0)
            );
        }
    }

    public static final class Point {
        public final String date;
        public final int totalViews;
        public final int internalRef;
        public final int searchRef;
        public final int externalRef;

        private Point(String date, int totalViews, int internalRef, int searchRef, int externalRef) {
            this.date = clean(date);
            this.totalViews = totalViews;
            this.internalRef = internalRef;
            this.searchRef = searchRef;
            this.externalRef = externalRef;
        }
    }
}
