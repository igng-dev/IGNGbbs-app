package com.example.igngbbs;

import java.util.List;

public final class PostPage {
    public final List<Post> posts;
    public final Integer nextCursor;

    PostPage(List<Post> posts, Integer nextCursor) {
        this.posts = posts;
        this.nextCursor = nextCursor;
    }
}
