package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CreatorPostDetail {
    public final Post post;
    public final String draftTitle;
    public final String draftContent;
    public final String draftUpdatedAt;
    public final String pendingReviewType;
    public final String pendingReviewStatus;
    public final String rejectionReason;
    public final String suggestion;
    public final boolean hasPendingAppeal;
    public final List<SeriesLink> seriesLinks;
    public final List<PostGroupLink> postGroups;

    public CreatorPostDetail(
            Post post,
            String draftTitle,
            String draftContent,
            String draftUpdatedAt,
            String pendingReviewType,
            String pendingReviewStatus,
            String rejectionReason,
            String suggestion,
            boolean hasPendingAppeal,
            List<SeriesLink> seriesLinks,
            List<PostGroupLink> postGroups
    ) {
        this.post = post;
        this.draftTitle = clean(draftTitle);
        this.draftContent = clean(draftContent);
        this.draftUpdatedAt = clean(draftUpdatedAt);
        this.pendingReviewType = clean(pendingReviewType);
        this.pendingReviewStatus = clean(pendingReviewStatus);
        this.rejectionReason = clean(rejectionReason);
        this.suggestion = clean(suggestion);
        this.hasPendingAppeal = hasPendingAppeal;
        this.seriesLinks = seriesLinks;
        this.postGroups = postGroups;
    }

    static CreatorPostDetail fromJson(JSONObject root) {
        JSONObject post = root.optJSONObject("post");
        JSONObject draft = root.optJSONObject("draft");
        JSONObject review = root.optJSONObject("pendingReview");
        JSONObject appeal = root.optJSONObject("pendingAppeal");
        String fallbackRejectionReason = post == null ? "" : post.optString("rejection_reason", "");
        String fallbackSuggestion = post == null ? "" : post.optString("suggestion", "");
        return new CreatorPostDetail(
                post == null ? null : Post.fromJson(post),
                draft == null ? "" : draft.optString("title", ""),
                draft == null ? "" : draft.optString("content", ""),
                draft == null ? "" : draft.optString("updated_at", ""),
                review == null ? "" : review.optString("review_type", ""),
                review == null ? "" : review.optString("status", ""),
                review == null ? fallbackRejectionReason : review.optString("rejection_reason", fallbackRejectionReason),
                review == null ? fallbackSuggestion : review.optString("suggestion", fallbackSuggestion),
                appeal != null,
                parseSeriesLinks(root.optJSONArray("seriesLinks")),
                parsePostGroups(root.optJSONArray("postGroups"))
        );
    }

    private static List<SeriesLink> parseSeriesLinks(JSONArray items) {
        List<SeriesLink> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            JSONObject series = item.optJSONObject("series");
            result.add(new SeriesLink(
                    item.optInt("id", 0),
                    item.optInt("order", i),
                    series == null ? 0 : series.optInt("id", 0),
                    series == null ? "" : series.optString("name", ""),
                    series == null ? "" : series.optString("slug", "")
            ));
        }
        return result;
    }

    private static List<PostGroupLink> parsePostGroups(JSONArray items) {
        List<PostGroupLink> result = new ArrayList<>();
        if (items == null) return result;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            JSONObject group = item.optJSONObject("group");
            result.add(new PostGroupLink(
                    item.optInt("id", 0),
                    item.optInt("group_id", group == null ? 0 : group.optInt("id", 0)),
                    group == null ? "" : group.optString("name", "")
            ));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }

    public static final class SeriesLink {
        public final int id;
        public final int order;
        public final int seriesId;
        public final String seriesName;
        public final String seriesSlug;

        SeriesLink(int id, int order, int seriesId, String seriesName, String seriesSlug) {
            this.id = id;
            this.order = order;
            this.seriesId = seriesId;
            this.seriesName = clean(seriesName);
            this.seriesSlug = clean(seriesSlug);
        }
    }

    public static final class PostGroupLink {
        public final int id;
        public final int groupId;
        public final String groupName;

        PostGroupLink(int id, int groupId, String groupName) {
            this.id = id;
            this.groupId = groupId;
            this.groupName = clean(groupName);
        }
    }
}
