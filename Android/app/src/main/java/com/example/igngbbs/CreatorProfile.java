package com.example.igngbbs;

import org.json.JSONObject;

public final class CreatorProfile {
    public final String username;
    public final String nickname;
    public final String bio;
    public final int bioStatus;
    public final String bioUpdatedAt;
    public final String reviewStatus;
    public final String rejectionReason;
    public final String suggestion;

    public CreatorProfile(
            String username,
            String nickname,
            String bio,
            int bioStatus,
            String bioUpdatedAt,
            String reviewStatus,
            String rejectionReason,
            String suggestion
    ) {
        this.username = clean(username);
        this.nickname = clean(nickname);
        this.bio = clean(bio);
        this.bioStatus = bioStatus;
        this.bioUpdatedAt = clean(bioUpdatedAt);
        this.reviewStatus = clean(reviewStatus);
        this.rejectionReason = clean(rejectionReason);
        this.suggestion = clean(suggestion);
    }

    static CreatorProfile fromJson(JSONObject root) {
        JSONObject profile = root.optJSONObject("profile");
        JSONObject review = root.optJSONObject("review");
        if (profile == null) profile = root;
        return new CreatorProfile(
                profile.optString("username", ""),
                profile.optString("nickname", ""),
                profile.optString("bio", ""),
                profile.optInt("bio_status", 0),
                profile.optString("bio_updated_at", ""),
                review == null ? "" : review.optString("status", ""),
                review == null ? "" : review.optString("rejection_reason", ""),
                review == null ? "" : review.optString("suggestion", "")
        );
    }

    public String displayName() {
        return nickname.isEmpty() ? username : nickname;
    }

    private static String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) return "";
        return value;
    }
}
