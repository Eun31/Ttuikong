package com.ttuikong.spring.model.dto;

import java.util.Date;

public class DailyRecord {
	private int id;
	private int userId;
	private Date date;
	private int exerciseTime;
	private float distance;
	private int calories;
	private String mood;
	private Integer routeId; // null값 허용
	private String createdAt;

	public DailyRecord() {
	}

	public DailyRecord(int id, int userId, Date date, int exerciseTime, float distance, int calories, String mood,
			Integer routeId, String createdAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.date = date;
		this.exerciseTime = exerciseTime;
		this.distance = distance;
		this.calories = calories;
		this.mood = mood;
		this.routeId = routeId;
		this.createdAt = createdAt;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getExerciseTime() {
		return exerciseTime;
	}

	public void setExerciseTime(int exerciseTime) {
		this.exerciseTime = exerciseTime;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public int getCalories() {
		return calories;
	}

	public void setCalories(int calories) {
		this.calories = calories;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public Integer getRouteId() {
		return routeId;
	}

	public void setRouteId(Integer routeId) {
		this.routeId = routeId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "DailyRecord [id=" + id + ", userId=" + userId + ", date=" + date + ", exerciseTime=" + exerciseTime
				+ ", distance=" + distance + ", calories=" + calories + ", mood=" + mood + ", routeId=" + routeId
				+ ", createdAt=" + createdAt + "]";
	}

}
