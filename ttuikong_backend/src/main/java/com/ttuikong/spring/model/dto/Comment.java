package com.ttuikong.spring.model.dto;

import java.time.LocalDateTime;

public class Comment {
    
    private int id;
    private int boardId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userNickname;
    private int commentCount;
    

	// 기본 생성자
    public Comment() {
    }

	public Comment(int id, int boardId, int userId, String content, LocalDateTime createdAt, LocalDateTime updatedAt,
			String userNickname) {
		super();
		this.id = id;
		this.boardId = boardId;
		this.userId = userId;
		this.content = content;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userNickname = userNickname;
	}

    // Getter와 Setter 메서드
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getBoardId() {
        return boardId;
    }
    
    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }
    
    public int getUserId() {
    	return userId;
    }
    
    public void setUserId(int userId) {
    	this.userId = userId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
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
    
    public String getUserNickname() {
    	return userNickname;
    }
    
    public void setUserNickname(String userNickname) {
    	this.userNickname = userNickname;
    }
    
    public int getCommentCount() {
    	return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
    	this.commentCount = commentCount;
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", boardId=" + boardId +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}