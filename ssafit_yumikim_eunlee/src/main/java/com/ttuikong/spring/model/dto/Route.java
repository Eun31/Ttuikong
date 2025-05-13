package com.ttuikong.spring.model.dto;

import java.util.List;

public class Route {
	private int id;
    private int userId;
    private String name;
    private String startTime;
    private String endTime;
    private double distance; // 킬로미터
    private long duration;   // 초 단위
    private double calories;
    private List<RoutePoint> points; // 전체 경로 포인트
    private String createdAt;
    
    public Route() {
	}

	public Route(int id, int userId, String name, String startTime, String endTime, double distance, long duration,
			double calories, List<RoutePoint> points, String createdAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.distance = distance;
		this.duration = duration;
		this.calories = calories;
		this.points = points;
		this.createdAt = createdAt;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
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

	@Override
	public String toString() {
		return "Route [id=" + id + ", userId=" + userId + ", name=" + name + ", startTime=" + startTime + ", endTime="
				+ endTime + ", distance=" + distance + ", duration=" + duration + ", points=" + points + ", createdAt="
				+ createdAt + "]";
	}
    
}
