package com.example.igngbbs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class BbsApiClient {
    private final String baseUrl;
    private final TokenProvider tokenProvider;

    public BbsApiClient(String baseUrl) {
        this(baseUrl, null);
    }

    public BbsApiClient(String baseUrl, TokenProvider tokenProvider) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.tokenProvider = tokenProvider;
    }

    public List<Post> fetchPosts(int limit) throws Exception {
        return fetchPosts(limit, null, null, null, null);
    }

    public List<Post> fetchPosts(int limit, Integer categoryId, String tag, Integer authorId, Integer columnId) throws Exception {
        return fetchPostsPage(limit, null, categoryId, tag, authorId, columnId).posts;
    }

    public PostPage fetchPostsPage(int limit, Integer cursor, Integer categoryId, String tag, Integer authorId, Integer columnId) throws Exception {
        List<Post> posts = new ArrayList<>();
        JSONObject root = getJson(buildPostsPath(limit, cursor, categoryId, tag, authorId, columnId));
        JSONArray items = root.optJSONArray("posts");
        if (items == null) {
            return new PostPage(posts, null);
        }
        for (int i = 0; i < items.length(); i++) {
            posts.add(Post.fromJson(items.getJSONObject(i)));
        }
        Integer nextCursor = root.isNull("nextCursor") ? null : root.optInt("nextCursor");
        return new PostPage(posts, nextCursor);
    }

    private String buildPostsPath(int limit, Integer cursor, Integer categoryId, String tag, Integer authorId, Integer columnId) throws Exception {
        StringBuilder path = new StringBuilder("/api/v1/posts?limit=").append(limit);
        if (cursor != null && cursor > 0) path.append("&cursor=").append(cursor);
        if (categoryId != null && categoryId > 0) path.append("&category_id=").append(categoryId);
        if (authorId != null && authorId > 0) path.append("&author_id=").append(authorId);
        if (columnId != null && columnId > 0) path.append("&column_id=").append(columnId);
        if (tag != null && !tag.trim().isEmpty()) {
            path.append("&tags=").append(URLEncoder.encode(tag.trim(), StandardCharsets.UTF_8.name()));
        }
        return path.toString();
    }

    public List<ColumnItem> fetchColumns() throws Exception {
        JSONObject root = getJson("/api/v1/columns");
        JSONArray items = root.optJSONArray("columns");
        List<ColumnItem> columns = new ArrayList<>();
        if (items == null) {
            return columns;
        }
        for (int i = 0; i < items.length(); i++) {
            columns.add(ColumnItem.fromJson(items.getJSONObject(i)));
        }
        return columns;
    }

    public List<CategoryItem> fetchCreatorCategories() throws Exception {
        JSONObject root = getJson("/api/mobile/creator/categories");
        JSONArray items = root.optJSONArray("categories");
        List<CategoryItem> categories = new ArrayList<>();
        if (items == null) {
            return categories;
        }
        for (int i = 0; i < items.length(); i++) {
            categories.add(CategoryItem.fromJson(items.getJSONObject(i)));
        }
        return categories;
    }

    public Post fetchPost(int id) throws Exception {
        JSONObject root = getJson("/api/v1/posts/" + id);
        JSONObject post = root.optJSONObject("post");
        if (post == null) {
            throw new IOException("响应缺少文章内容");
        }
        return Post.fromJson(post);
    }

    public List<Post> searchPosts(String query, int limit, Integer categoryId, String tag, Integer authorId, Integer columnId) throws Exception {
        StringBuilder path = new StringBuilder("/api/v1/posts/search?q=")
                .append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()))
                .append("&limit=").append(limit);
        if (categoryId != null && categoryId > 0) path.append("&category_id=").append(categoryId);
        if (tag != null && !tag.trim().isEmpty()) path.append("&tags=").append(URLEncoder.encode(tag.trim(), StandardCharsets.UTF_8.name()));
        if (authorId != null && authorId > 0) path.append("&author_id=").append(authorId);
        if (columnId != null && columnId > 0) path.append("&column_id=").append(columnId);
        JSONObject root = getJson(path.toString());
        JSONArray items = root.optJSONArray("posts");
        List<Post> posts = new ArrayList<>();
        if (items == null) {
            return posts;
        }
        for (int i = 0; i < items.length(); i++) {
            posts.add(Post.fromJson(items.getJSONObject(i)));
        }
        return posts;
    }

    public NotificationPage fetchNotifications(String limit, boolean unreadOnly) throws Exception {
        StringBuilder path = new StringBuilder("/api/v1/notifications?");
        path.append("limit=").append(URLEncoder.encode(limit == null || limit.trim().isEmpty() ? "20" : limit.trim(), StandardCharsets.UTF_8.name()));
        if (unreadOnly) {
            path.append("&unread=1");
        }
        return NotificationPage.fromJson(getJson(path.toString()));
    }

    public int fetchUnreadNotificationCount() throws Exception {
        JSONObject root = getJson("/api/v1/notifications/unread-count");
        return root.optInt("total", root.optInt("notifications", 0));
    }

    public List<PostHistoryItem> fetchHistory() throws Exception {
        JSONObject root = getJson("/api/v1/history");
        JSONArray items = root.optJSONArray("history");
        List<PostHistoryItem> history = new ArrayList<>();
        if (items == null) {
            return history;
        }
        for (int i = 0; i < items.length(); i++) {
            history.add(PostHistoryItem.fromJson(items.getJSONObject(i)));
        }
        return history;
    }

    public PostHistoryItem fetchPostHistory(int postId) throws Exception {
        JSONObject root = getJson("/api/v1/posts/" + postId + "/history");
        JSONObject history = root.optJSONObject("history");
        if (history == null) {
            return null;
        }
        return PostHistoryItem.fromJson(history);
    }

    public void deleteHistory(int historyId) throws Exception {
        requestJson("DELETE", "/api/v1/history/" + historyId, null);
    }

    public void recordPostView(int postId) throws Exception {
        requestJson("POST", "/api/v1/posts/" + postId + "/view", new JSONObject());
    }

    public void savePostProgress(int postId, String progressAnchor) throws Exception {
        requestJson("POST", "/api/v1/posts/" + postId + "/view", new JSONObject()
                .put("incrementView", false)
                .put("updateProgress", true)
                .put("progressAnchor", progressAnchor == null ? "" : progressAnchor));
    }

    public void clearPostProgress(int postId) throws Exception {
        requestJson("POST", "/api/v1/posts/" + postId + "/view", new JSONObject()
                .put("incrementView", false)
                .put("clearProgress", true));
    }

    public void markNotificationRead(int recipientId) throws Exception {
        requestJson("PUT", "/api/v1/notifications/" + recipientId + "/read", null);
    }

    public void markAllNotificationsRead() throws Exception {
        requestJson("PUT", "/api/v1/notifications/read-all", null);
    }

    public UserProfile fetchUserProfile(int userId) throws Exception {
        return UserProfile.fromJson(getJson("/api/v1/user/" + userId + "/profile"));
    }

    public LikeState fetchPostLike(int postId) throws Exception {
        return LikeState.fromJson(getJson("/api/v1/posts/" + postId + "/like"));
    }

    public LikeState togglePostLike(int postId) throws Exception {
        return LikeState.fromJson(requestJson("POST", "/api/v1/posts/" + postId + "/like", null));
    }

    public LikeState toggleCommentLike(int commentId) throws Exception {
        return LikeState.fromJson(requestJson("POST", "/api/v1/comments/" + commentId + "/like", null));
    }

    public void report(String targetType, int targetId, String reason, String contentSnapshot, String titleSnapshot) throws Exception {
        JSONObject body = new JSONObject()
                .put("target_type", targetType)
                .put("target_id", targetId);
        if (reason != null && !reason.trim().isEmpty()) {
            body.put("reason", reason.trim());
        }
        if (contentSnapshot != null && !contentSnapshot.trim().isEmpty()) {
            body.put("content_snapshot", contentSnapshot);
        }
        if (titleSnapshot != null && !titleSnapshot.trim().isEmpty()) {
            body.put("title_snapshot", titleSnapshot);
        }
        requestJson("POST", "/api/v1/review/report", body);
    }

    public List<CommentItem> fetchComments(int postId) throws Exception {
        JSONObject root = getJson("/api/comments?postId=" + postId);
        JSONArray items = root.optJSONArray("comments");
        List<CommentItem> comments = new ArrayList<>();
        if (items == null) {
            return comments;
        }
        for (int i = 0; i < items.length(); i++) {
            comments.add(CommentItem.fromJson(items.getJSONObject(i)));
        }
        return comments;
    }

    public CommentItem createComment(int postId, String content, Integer parentId) throws Exception {
        JSONObject body = new JSONObject()
                .put("postId", postId)
                .put("content", content);
        if (parentId != null && parentId > 0) {
            body.put("parentId", parentId);
        }
        JSONObject root = requestJson("POST", "/api/comments", body);
        JSONObject comment = root.optJSONObject("comment");
        if (comment == null) {
            throw new IOException("响应缺少评论内容");
        }
        return CommentItem.fromJson(comment);
    }

    public CheckinState fetchMobileCheckin(String token) throws Exception {
        return CheckinState.fromJson(requestJson("GET", "/api/mobile/checkin", null, token));
    }

    public CheckinState submitMobileCheckin(String token) throws Exception {
        return CheckinState.fromJson(requestJson("POST", "/api/mobile/checkin", null, token));
    }

    public CreatorOverview fetchCreatorOverview() throws Exception {
        return CreatorOverview.fromJson(getJson("/api/mobile/creator/overview"));
    }

    public CreatorAnalytics fetchCreatorAnalytics(String type, Integer id, int year, int month) throws Exception {
        StringBuilder path = new StringBuilder("/api/mobile/creator/analytics?type=")
                .append(URLEncoder.encode(type == null ? "posts" : type, StandardCharsets.UTF_8.name()))
                .append("&year=").append(year)
                .append("&month=").append(month);
        if (id != null && id > 0) {
            path.append("&id=").append(id);
        }
        return CreatorAnalytics.fromJson(getJson(path.toString()));
    }

    public List<Post> fetchCreatorPosts(Integer status, Integer banStatus, int limit, String query) throws Exception {
        StringBuilder path = new StringBuilder("/api/mobile/creator/posts?limit=").append(limit);
        if (status != null) {
            path.append("&status=").append(status);
        }
        if (banStatus != null) {
            path.append("&ban_status=").append(banStatus);
        }
        if (query != null && !query.trim().isEmpty()) {
            path.append("&q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()));
        }
        JSONObject root = getJson(path.toString());
        JSONArray items = root.optJSONArray("posts");
        List<Post> posts = new ArrayList<>();
        if (items == null) {
            return posts;
        }
        for (int i = 0; i < items.length(); i++) {
            posts.add(Post.fromJson(items.getJSONObject(i)));
        }
        return posts;
    }

    public void deleteCreatorPost(int id) throws Exception {
        JSONObject body = new JSONObject().put("id", id);
        requestJson("DELETE", "/api/mobile/creator/posts", body);
    }

    public CreatorPostDetail fetchCreatorPostDetail(int id) throws Exception {
        return CreatorPostDetail.fromJson(getJson("/api/mobile/creator/posts/" + id));
    }

    public int createCreatorPost(String title, String content, int categoryId, String tags, int aiUsage, String format) throws Exception {
        JSONObject body = new JSONObject()
                .put("title", title)
                .put("content", content == null ? "" : content)
                .put("category_id", categoryId)
                .put("tags", tags == null ? "" : tags)
                .put("ai_usage", aiUsage)
                .put("format", format == null || format.trim().isEmpty() ? "markdown" : format.trim())
                .put("status", 1);
        JSONObject root = requestJson("POST", "/api/mobile/creator/posts", body);
        return root.optInt("post_id", root.optJSONObject("post") == null ? 0 : root.optJSONObject("post").optInt("post_id", 0));
    }

    public void updateCreatorPost(int id, String title, String content, String tags, Integer categoryId, Integer columnId, String visibility, String groupFilterMode, boolean submitForReview, boolean requestTags, boolean requestSummary) throws Exception {
        updateCreatorPostFull(id, title, content, tags, categoryId, columnId, null, visibility, groupFilterMode, null, submitForReview, requestTags, requestSummary);
    }

    public void updateCreatorPostFull(int id, String title, String content, String tags, Integer categoryId, Integer columnId, Integer aiUsage, String visibility, String groupFilterMode, List<Integer> groupIds, boolean submitForReview, boolean requestTags, boolean requestSummary) throws Exception {
        JSONObject body = new JSONObject();
        if (title != null) body.put("title", title);
        if (content != null) body.put("content", content);
        if (tags != null) body.put("tags", tags);
        if (categoryId != null) body.put("category_id", categoryId);
        if (columnId != null) body.put("column_id", columnId);
        if (aiUsage != null) body.put("ai_usage", aiUsage);
        if (visibility != null) body.put("visibility", visibility);
        if (groupFilterMode != null) body.put("group_filter_mode", groupFilterMode);
        if (groupIds != null) {
            JSONArray ids = new JSONArray();
            for (Integer groupId : groupIds) {
                if (groupId != null && groupId > 0) ids.put(groupId);
            }
            body.put("groupIds", ids);
        }
        body.put("submit_for_review", submitForReview);
        body.put("request_tags", requestTags);
        body.put("request_summary", requestSummary);
        requestJson("PUT", "/api/mobile/creator/posts/" + id, body);
    }

    public void updateCreatorPostSettings(int id, String title, String content, String tags, Integer categoryId, Integer columnId, String visibility, String groupFilterMode, boolean submitForReview, boolean requestTags, boolean requestSummary) throws Exception {
        updateCreatorPost(id, title, content, tags, categoryId, columnId, visibility, groupFilterMode, submitForReview, requestTags, requestSummary);
    }

    public List<CreatorAppeal> fetchCreatorAppeals() throws Exception {
        JSONObject root = getJson("/api/mobile/creator/appeals");
        JSONArray items = root.optJSONArray("appeals");
        List<CreatorAppeal> appeals = new ArrayList<>();
        if (items == null) {
            return appeals;
        }
        for (int i = 0; i < items.length(); i++) {
            appeals.add(CreatorAppeal.fromJson(items.getJSONObject(i)));
        }
        return appeals;
    }

    public CreatorAppeal submitCreatorAppeal(String targetType, int targetId, String reason) throws Exception {
        JSONObject body = new JSONObject()
                .put("target_type", targetType)
                .put("target_id", targetId)
                .put("reason", reason);
        return CreatorAppeal.fromJson(requestJson("POST", "/api/mobile/creator/appeals", body));
    }

    public CreatorPermissionsState fetchCreatorPermissions() throws Exception {
        return CreatorPermissionsState.fromJson(getJson("/api/mobile/creator/permissions"));
    }

    public CreatorPersonalization fetchCreatorPersonalization() throws Exception {
        return CreatorPersonalization.fromJson(getJson("/api/mobile/creator/personalization"));
    }

    public void saveCreatorPersonalization(String excludedCats, String excludedTags, String excludedUsers, boolean hideAi) throws Exception {
        JSONObject body = new JSONObject()
                .put("excluded_cats", excludedCats == null ? "" : excludedCats)
                .put("excluded_tags", excludedTags == null ? "" : excludedTags)
                .put("excluded_users", excludedUsers == null ? "" : excludedUsers)
                .put("hide_ai", hideAi);
        requestJson("POST", "/api/mobile/creator/personalization", body);
    }

    public List<CreatorGroup> fetchCreatorGroups() throws Exception {
        JSONObject root = getJson("/api/mobile/creator/groups");
        return CreatorGroup.listFromJson(root.optJSONArray("groups"));
    }

    public CreatorDailyLimit fetchCreatorDailyLimit() throws Exception {
        return CreatorDailyLimit.fromJson(getJson("/api/mobile/creator/review/daily-limit"));
    }

    public CreatorGroup createCreatorGroup(String name) throws Exception {
        JSONObject body = new JSONObject().put("name", name);
        JSONObject root = requestJson("POST", "/api/mobile/creator/groups", body);
        JSONObject group = root.optJSONObject("group");
        if (group == null) return null;
        JSONArray items = new JSONArray().put(group);
        List<CreatorGroup> groups = CreatorGroup.listFromJson(items);
        return groups.isEmpty() ? null : groups.get(0);
    }

    public void renameCreatorGroup(int id, String name) throws Exception {
        JSONObject body = new JSONObject().put("name", name);
        requestJson("PATCH", "/api/mobile/creator/groups/" + id, body);
    }

    public void deleteCreatorGroup(int id) throws Exception {
        requestJson("DELETE", "/api/mobile/creator/groups/" + id, null);
    }

    public void addCreatorGroupMember(int id, String username) throws Exception {
        JSONObject body = new JSONObject().put("username", username);
        requestJson("POST", "/api/mobile/creator/groups/" + id + "/members", body);
    }

    public void removeCreatorGroupMember(int id, int userId) throws Exception {
        requestJson("DELETE", "/api/mobile/creator/groups/" + id + "/members?userId=" + userId, null);
    }

    public List<CreatorGroup.GroupMember> searchCreatorUsers(String query) throws Exception {
        JSONObject root = getJson("/api/mobile/creator/users/search?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()));
        JSONArray items = root.optJSONArray("users");
        return CreatorGroup.GroupMember.listFromJson(items);
    }

    public List<CreatorColumn> fetchCreatorColumns() throws Exception {
        JSONObject root = getJson("/api/mobile/creator/columns");
        JSONArray items = root.optJSONArray("columns");
        List<CreatorColumn> columns = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                columns.add(CreatorColumn.fromJson(items.getJSONObject(i)));
            }
        }
        return columns;
    }

    public List<ColumnItem> searchColumns(String query) throws Exception {
        JSONObject root = getJson("/api/v1/columns/search?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()));
        JSONArray items = root.optJSONArray("columns");
        List<ColumnItem> columns = new ArrayList<>();
        if (items == null) return columns;
        for (int i = 0; i < items.length(); i++) {
            columns.add(ColumnItem.fromJson(items.getJSONObject(i)));
        }
        return columns;
    }

    public CreatorColumn fetchCreatorColumnDetail(int id) throws Exception {
        return CreatorColumn.fromJson(getJson("/api/mobile/creator/columns/" + id));
    }

    public CreatorColumn createCreatorColumn(String name, String slug, String description) throws Exception {
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("slug", slug)
                .put("description", description == null ? "" : description);
        return CreatorColumn.fromJson(requestJson("POST", "/api/mobile/creator/columns", body));
    }

    public void updateCreatorColumn(int id, String name, String slug, String description) throws Exception {
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("slug", slug)
                .put("description", description == null ? "" : description);
        requestJson("PUT", "/api/mobile/creator/columns/" + id, body);
    }

    public void updateCreatorColumnConfig(int id, String name, String slug, String description, Boolean visitorPostAllowed, String visitorViewScope, String visitorEditScope, Boolean managerPostAllowed, String managerViewScope, String managerEditScope) throws Exception {
        updateCreatorColumnMobile(
                id,
                name,
                slug,
                description,
                visitorPostAllowed,
                visitorViewScope,
                visitorEditScope,
                managerPostAllowed,
                managerViewScope,
                managerEditScope
        );
    }

    public void updateCreatorColumnMobile(int id, String name, String slug, String description, Boolean visitorPostAllowed, String visitorViewScope, String visitorEditScope, Boolean managerPostAllowed, String managerViewScope, String managerEditScope) throws Exception {
        JSONObject body = new JSONObject();
        if (name != null) body.put("name", name);
        if (slug != null) body.put("slug", slug);
        if (description != null) body.put("description", description);
        if (visitorPostAllowed != null) body.put("visitor_post_allowed", visitorPostAllowed);
        if (visitorViewScope != null) body.put("visitor_view_scope", visitorViewScope);
        if (visitorEditScope != null) body.put("visitor_edit_scope", visitorEditScope);
        if (managerPostAllowed != null) body.put("manager_post_allowed", managerPostAllowed);
        if (managerViewScope != null) body.put("manager_view_scope", managerViewScope);
        if (managerEditScope != null) body.put("manager_edit_scope", managerEditScope);
        requestJson("PUT", "/api/mobile/creator/columns/" + id, body);
    }

    public void addCreatorColumnManager(int id, int userId) throws Exception {
        JSONObject body = new JSONObject().put("user_id", userId);
        requestJson("POST", "/api/v1/columns/" + id + "/managers", body);
    }

    public List<CreatorSeries> fetchCreatorSeries() throws Exception {
        JSONObject root = getJson("/api/mobile/creator/series");
        JSONArray items = root.optJSONArray("seriesList");
        List<CreatorSeries> series = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                series.add(CreatorSeries.fromJson(items.getJSONObject(i)));
            }
        }
        return series;
    }

    public CreatorSeries fetchCreatorSeriesDetail(int id) throws Exception {
        return CreatorSeries.fromJson(getJson("/api/mobile/creator/series/" + id));
    }

    public CreatorSeries createCreatorSeries(String name, String slug, String description) throws Exception {
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("slug", slug)
                .put("description", description == null ? "" : description);
        return CreatorSeries.fromJson(requestJson("POST", "/api/mobile/creator/series", body));
    }

    public void updateCreatorSeries(int id, String name, String slug, String description) throws Exception {
        JSONObject body = new JSONObject()
                .put("name", name)
                .put("slug", slug)
                .put("description", description == null ? "" : description);
        requestJson("PUT", "/api/mobile/creator/series/" + id, body);
    }

    public void updateCreatorSeriesDetails(int id, String name, String slug, String description) throws Exception {
        updateCreatorSeries(id, name, slug, description);
    }

    public List<Post> fetchCreatorSeriesPostList(int id) throws Exception {
        JSONObject root = getJson("/api/mobile/creator/series/" + id + "/posts");
        JSONArray items = root.optJSONArray("posts");
        List<Post> posts = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item != null) {
                    posts.add(Post.fromJson(item));
                }
            }
        }
        return posts;
    }

    public List<CreatorSeriesPostCandidate> fetchCreatorSeriesPostCandidates(int id) throws Exception {
        JSONObject root = getJson("/api/mobile/creator/series/" + id + "/posts");
        JSONArray items = root.optJSONArray("posts");
        List<CreatorSeriesPostCandidate> posts = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                posts.add(CreatorSeriesPostCandidate.fromJson(items.getJSONObject(i)));
            }
        }
        return posts;
    }

    public void addPostToCreatorSeries(int seriesId, int postId) throws Exception {
        JSONObject body = new JSONObject().put("post_id", postId);
        requestJson("POST", "/api/mobile/creator/series/" + seriesId + "/posts", body);
    }

    public void removePostFromCreatorSeries(int seriesId, int postId) throws Exception {
        requestJson("DELETE", "/api/mobile/creator/series/" + seriesId + "/posts/" + postId, null);
    }

    public void reorderCreatorSeriesPosts(int seriesId, List<CreatorSeries.SeriesPostItem> items) throws Exception {
        JSONArray postOrders = new JSONArray();
        for (int i = 0; i < items.size(); i++) {
            postOrders.put(new JSONObject()
                    .put("post_id", items.get(i).postId)
                    .put("order", i + 1));
        }
        JSONObject body = new JSONObject().put("postOrders", postOrders);
        requestJson("PATCH", "/api/mobile/creator/series/" + seriesId + "/posts", body);
    }

    public void deleteCreatorSeries(int id) throws Exception {
        requestJson("DELETE", "/api/mobile/creator/series/" + id, null);
    }

    public List<CreatorComment> fetchCreatorComments(String mode, int limit) throws Exception {
        StringBuilder path = new StringBuilder("/api/mobile/creator/comments?limit=").append(limit);
        if (mode != null && !mode.trim().isEmpty()) {
            path.append("&mode=").append(URLEncoder.encode(mode.trim(), StandardCharsets.UTF_8.name()));
        }
        JSONObject root = getJson(path.toString());
        JSONArray items = root.optJSONArray("comments");
        List<CreatorComment> comments = new ArrayList<>();
        if (items == null) {
            return comments;
        }
        for (int i = 0; i < items.length(); i++) {
            comments.add(CreatorComment.fromJson(items.getJSONObject(i)));
        }
        return comments;
    }

    public void markMentionRead(int mentionId) throws Exception {
        requestJson("PUT", "/api/v1/mentions/" + mentionId + "/read", null);
    }

    public void markAllMentionsRead() throws Exception {
        requestJson("PUT", "/api/v1/mentions/read-all", null);
    }

    public void deleteCreatorComment(int id) throws Exception {
        JSONObject body = new JSONObject().put("id", id);
        requestJson("DELETE", "/api/mobile/creator/comments", body);
    }

    public CreatorProfile fetchCreatorProfile() throws Exception {
        return CreatorProfile.fromJson(getJson("/api/mobile/creator/profile"));
    }

    public void updateCreatorProfile(String bio) throws Exception {
        JSONObject body = new JSONObject().put("bio", bio);
        requestJson("POST", "/api/mobile/creator/profile", body);
    }

    public JSONObject fetchCreatorReviewCenter() throws Exception {
        return getJson("/api/mobile/creator/review");
    }

    private JSONObject getJson(String path) throws Exception {
        return requestJson("GET", path, null);
    }

    private JSONObject requestJson(String method, String path, JSONObject payload) throws Exception {
        return requestJson(method, path, payload, null);
    }

    private JSONObject requestJson(String method, String path, JSONObject payload, String explicitToken) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("User-Agent", "IGNGbbs-Android/1.0");
            String token = explicitToken != null ? explicitToken : (tokenProvider == null ? null : tokenProvider.getToken());
            if (token != null && !token.trim().isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token.trim());
            }
            if (payload != null && !"GET".equals(method)) {
                conn.setDoOutput(true);
                try (OutputStream stream = conn.getOutputStream()) {
                    stream.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            String body = readFully(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            if (code >= 400) {
                JSONObject errorJson = body.isEmpty() ? new JSONObject() : new JSONObject(body);
                String message = errorJson.optString("error", "请求失败：" + code);
                String rejectionReason = errorJson.optString("rejection_reason", "").trim();
                String suggestion = errorJson.optString("suggestion", "").trim();
                if (!rejectionReason.isEmpty()) {
                    message += "\n\n原因：" + rejectionReason;
                }
                if (!suggestion.isEmpty()) {
                    message += "\n建议：" + suggestion;
                }
                throw new ApiException(code, errorJson.optString("error_code", ""), message);
            }
            return new JSONObject(body);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
