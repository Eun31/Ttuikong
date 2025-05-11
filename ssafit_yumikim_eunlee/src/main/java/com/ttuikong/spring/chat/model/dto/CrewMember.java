package com.ttuikong.spring.chat.model.dto;

import java.time.LocalDateTime;

public class CrewMember {
	private int id;
	private int crewId;
	private int userId;
	private LocalDateTime joinedAt;

	CrewMember() {
	}

	public CrewMember(int id, int crewId, int userId, LocalDateTime joinedAt) {
		super();
		this.id = id;
		this.crewId = crewId;
		this.userId = userId;
		this.joinedAt = joinedAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCrewId() {
		return crewId;
	}

	public void setCrewId(int crewId) {
		this.crewId = crewId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public LocalDateTime getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(LocalDateTime joinedAt) {
		this.joinedAt = joinedAt;
	}

	@Override
	public String toString() {
		return "ChatMember [id=" + id + ", crewId=" + crewId + ", userId=" + userId + ", joinedAt=" + joinedAt + "]";
	}

}
