package com.ttuikong.spring.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class Route {
	private int id;
	private int userId;
	private String routeName;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private double distance; // 킬로미터
	private long duration; // 초 단위
	private List<RoutePoint> points; // 전체 경로 포인트
	private String createdAt;

	private String status;
	private String imageUrl;

	public Route() {
	}

	public Route(int id, int userId, String routeName, LocalDateTime startTime, LocalDateTime endTime, double distance,
			long duration,
			List<RoutePoint> points, String createdAt, String status, String imageUrl) {
		this.id = id;
		this.userId = userId;
		this.routeName = routeName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.distance = distance;
		this.duration = duration;
		this.calories = calories;
		this.points = points;
		this.createdAt = createdAt;
		this.status = status;
		this.imageUrl = imageUrl;
	}

	public double getCalories() {
		return calories;
	}

	public void setCalories(double calories) {
		this.calories = calories;
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

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public List<RoutePoint> getPoints() {
		return points;
	}

	public void setPoints(List<RoutePoint> points) {
		this.points = points;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public String toString() {
		return "Route [id=" + id + ", userId=" + userId + ", routeName=" + routeName + ", startTime=" + startTime
				+ ", endTime="
				+ endTime + ", distance=" + distance + ", duration=" + duration + ", points=" + points + ", createdAt="
				+ createdAt + ", status=" + status + ", imageUrl=" + imageUrl + "]";
	}

}