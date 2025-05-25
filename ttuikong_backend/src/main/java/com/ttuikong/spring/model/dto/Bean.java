package com.ttuikong.spring.model.dto;

public class Bean {
    private int id;
    private int userId;
    private int beans; 
    private String updatedAt;

    public Bean() {}
    
    public Bean(int id, int userId, int beans, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.beans = beans;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBeans() {
        return beans;
    }

    public void setBeans(int beans) {
        this.beans = beans;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Bean [id=" + id + ", userId=" + userId + ", beans=" + beans + ", updatedAt=" + updatedAt + "]";
    }
}
