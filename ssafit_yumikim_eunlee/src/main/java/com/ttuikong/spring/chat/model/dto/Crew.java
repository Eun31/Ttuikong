package com.ttuikong.spring.chat.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Date;

public class Crew {
	private int id;
	private String roomName;
	private LocalDateTime createdAt;
	private LocalDate startDate;
	private LocalDate endDate;
	private int creatorId;
	private String roomDescription;
	private String goalType;
	private Double goalTime;

	public Crew() {
	}

	public Crew(int id, String roomName, LocalDateTime createdAt, LocalDate startDate, LocalDate endDate, int creatorId,
			String roomDescription, String goalType, Double goalTime) {
		super();
		this.id = id;
		this.roomName = roomName;
		this.createdAt = createdAt;
		this.endDate = endDate;
		this.startDate = startDate;
		this.creatorId = creatorId;
		this.roomDescription = roomDescription;
		this.goalType = goalType;
		this.goalTime = goalTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public String getRoomDescription() {
		return roomDescription;
	}

	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	public String getGoalType() {
		return goalType;
	}

	public void setGoalType(String goalType) {
		this.goalType = goalType;
	}

	public Double getGoalTime() {
		return goalTime;
	}

	public void setGoalTime(Double goalTime) {
		this.goalTime = goalTime;
	}

	@Override
	public String toString() {
		return "Crew [id=" + id + ", roomName=" + roomName + ", createdAt=" + createdAt + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", creatorId=" + creatorId + ", roomDescription=" + roomDescription
				+ ", goalType=" + goalType + ", goalTime=" + goalTime + "]";
	}

}
