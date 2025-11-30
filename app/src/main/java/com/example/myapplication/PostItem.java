package com.example.myapplication;

import java.io.Serializable;

public class PostItem implements Serializable {
    public int id;
    public String imageUrl;
    public String title;
    public String avatarUrl;
    public String userName;
    public int likeCount;
    public boolean isLiked;
    public String description;

    public PostItem(int id, String imageUrl, String title, String userName, int likeCount, boolean isLiked, String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.userName = userName;
        this.likeCount = likeCount;
        this.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + userName;
        this.isLiked = isLiked;
        this.description = description;
    }
}