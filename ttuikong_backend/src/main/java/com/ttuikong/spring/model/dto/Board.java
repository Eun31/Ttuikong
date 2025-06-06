package com.ttuikong.spring.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private int postId;
    private int userId;
    private String userNickname; 
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
    
    public Board() {
        this.category = "자유";
    }
    
    public Board(int userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.category = "자유";
    }
    
    public Board(int postId, Integer userId, String title, String content, 
                    String category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Board(int postId, Integer userId, String title, String content, 
                    String category, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String imageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageUrl = imageUrl;
    }
    
    public Board(int postId, int userId, String userNickname, String title, String content, String category,
			LocalDateTime createdAt, LocalDateTime updatedAt, String imageUrl) {
		super();
		this.postId = postId;
		this.userId = userId;
		this.userNickname = userNickname;
		this.title = title;
		this.content = content;
		this.category = category;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.imageUrl = imageUrl;
	}

	public Integer getPostId() {
        return postId;
    }
    
    public void setPostId(int postId) {
        this.postId = postId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUserNickname() {
    	return userNickname;
    }
    
    public void setUserNickname(String userNickname) {
    	this.userNickname = userNickname;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    
    @Override
    public String toString() {
        return "BoardDto{" +
                "id=" + postId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", imageUrl=" + imageUrl +
                '}';
    }
}