package dev.nicolas.firebasedemo.model;

public class Notification {
    private String userId;
    private String text;
    private String postId;
    private boolean post;

    public Notification() {
    }

    public Notification(String userId, String text, String postId, boolean post) {
        this.userId = userId;
        this.text = text;
        this.postId = postId;
        this.post = post;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public boolean isPost() {
        return post;
    }

    public void setPost(boolean post) {
        this.post = post;
    }
}
