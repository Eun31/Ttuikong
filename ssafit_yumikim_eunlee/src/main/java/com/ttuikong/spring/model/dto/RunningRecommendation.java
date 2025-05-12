package com.ttuikong.spring.model.dto;

import java.time.LocalDateTime;

public class RunningRecommendation {
	private int id;
	private int userId;
	private double recommendedDistance;
	private int estimatedTime;
	private int estimatedCalories;
	private double averagePace;
	private double speed;
	private double improvement;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
	private boolean followed;
	
	public RunningRecommendation() {
		this.createdAt = LocalDateTime.now();
		this.followed = false;
	}

	public RunningRecommendation(int id, int userId, double recommendedDistance, int estimatedTime,
			int estimatedCalories, double averagePace, double speed, double improvement,
			LocalDateTime createdAt, boolean followed) {
		super();
		this.id = id;
		this.userId = userId;
		this.recommendedDistance = recommendedDistance;
		this.estimatedTime = estimatedTime;
		this.estimatedCalories = estimatedCalories;
		this.averagePace = averagePace;
		this.speed = speed;
		this.improvement = improvement;
		this.createdAt = createdAt;
		this.followed = followed;
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

	public double getRecommendedDistance() {
		return recommendedDistance;
	}

	public void setRecommendedDistance(double recommendedDistance) {
		this.recommendedDistance = recommendedDistance;
	}

	public int getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(int estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public int getEstimatedCalories() {
		return estimatedCalories;
	}

	public void setEstimatedCalories(int estimatedCalories) {
		this.estimatedCalories = estimatedCalories;
	}

	public double getAveragePace() {
		return averagePace;
	}

	public void setAveragePace(double averagePace) {
		this.averagePace = averagePace;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getImprovement() {
		return improvement;
	}

	public void setImprovement(double improvement) {
		this.improvement = improvement;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

	public boolean isFollowed() {
		return followed;
	}

	public void setFollowed(boolean followed) {
		this.followed = followed;
	}
	
	public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

	@Override
	public String toString() {
		return "RunningRecommendation [id=" + id + ", userId=" + userId + ", recommendedDistance=" + recommendedDistance
				+ ", estimatedTime=" + estimatedTime + ", estimatedCalories=" + estimatedCalories + ", averagePace="
				+ averagePace + ", speed=" + speed + ", improvement=" + improvement + ", createdAt=" + createdAt 
				+ ", followed=" + followed + "]";
	}
	
}
