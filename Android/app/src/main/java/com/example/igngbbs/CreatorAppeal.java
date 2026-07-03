package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorAppeal {
    public final int id;
    public final String targetType;
    public final int targetId;
    public final String reason;
    public final String status;
    public final String createdAt;
    public final String updatedAt;
    public final String targetTitle;
    public final String targetContent;
    public final String reviewType;
    public final String reviewStatus;
    public final String rejectionReason;
    public final String suggestion;
    public final String reporterName;
    public final String titleSnapshot;
    public final String contentSnapshot;
    public final int targetPostId;
    public final int targetBanStatus;

    public CreatorAppeal(
            int id,
            String targetType,
            int targetId,
            String reason,
            String status,
            String createdAt,
            String updatedAt,
            String targetTitle,
            String targetContent,
            String reviewType,
            String reviewStatus,
            String rejectionReason,
            String suggestion,
            String reporterName,
            String titleSnapshot,
            String contentSnapshot,
            int targetPostId,
            int targetBanStatus
    ) {
        this.id = id;
        this.targetType = clean(targetType);
        this.targetId = targetId;
        this.reason = clean(reason);
        this.status = clean(status);
        this.createdAt = clean(createdAt);
        this.updatedAt = clean(updatedAt);
        this.targetTitle = clean(targetTitle);
        this.targetContent = clean(targetContent);
        this.reviewType = clean(reviewType);
        this.reviewStatus = clean(reviewStatus);
        this.rejectionReason = clean(rejectionReason);
        this.suggestion = clean(suggestion);
        this.reporterName = clean(reporterName);
        this.titleSnapshot = clean(titleSnapshot);
        this.contentSnapshot = clean(contentSnapshot);
        this.targetPostId = targetPostId;
        this.targetBanStatus = targetBanStatus;
    }

    static CreatorAppeal fromJson(JSONObject root) {
        JSONObject appeal = root.optJSONObject("appeal");
        if (appeal == null) {
            appeal = root;
        }
        JSONObject target = appeal.optJSONObject("target");
        JSONObject review = appeal.optJSONObject("review");
        JSONObject reporter = appeal.optJSONObject("reporter");
        JSONObject targetUser = target == null ? null : target.optJSONObject("user");
        String targetTitle = target == null ? "" : target.optString("title", "");
        String targetContent = target == null ? "" : target.optString("content", "");
        if (targetTitle.isEmpty()) {
            targetTitle = appeal.optString("title_snapshot", "");
        }
        if (targetContent.isEmpty()) {
            targetContent = appeal.optString("content_snapshot", "");
        }
        return new CreatorAppeal(
                appeal.optInt("id", 0),
                appeal.optString("target_type", ""),
                appeal.optInt("target_id", 0),
                firstNonBlank(
                        appeal.optString("reason", ""),
                        appeal.optString("payload_reason", ""),
                        review == null ? "" : review.optString("reason", "")
                ),
                appeal.optString("status", ""),
                appeal.optString("created_at", ""),
                appeal.optString("updated_at", ""),
                targetTitle,
                targetContent,
                firstNonBlank(
                        appeal.optString("review_type", ""),
                        review == null ? "" : review.optString("review_type", "")
                ),
                firstNonBlank(
                        review == null ? "" : review.optString("status", ""),
                        appeal.optString("status", "")
                ),
                firstNonBlank(
                        review == null ? "" : review.optString("rejection_reason", ""),
                        appeal.optString("rejection_reason", "")
                ),
                firstNonBlank(
                        review == null ? "" : review.optString("suggestion", ""),
                        appeal.optString("suggestion", "")
                ),
                reporter == null ? "" : firstNonBlank(reporter.optString("nickname", ""), reporter.optString("username", "")),
                appeal.optString("title_snapshot", ""),
                appeal.optString("content_snapshot", ""),
                target == null ? 0 : target.optInt("post_id", 0),
                target == null ? 0 : target.optInt("ban_status", 0)
        );
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String cleaned = clean(value);
            if (!cleaned.isEmpty()) return cleaned;
        }
        return "";
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
