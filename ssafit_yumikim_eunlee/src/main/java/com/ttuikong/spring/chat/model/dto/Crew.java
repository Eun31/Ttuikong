package com.ttuikong.spring.chat.model.dto;

import java.time.LocalDateTime;

public class Crew {
	private int id;
	private String roomName;
	private LocalDateTime createdAt;
	private int memberCount;

	public Crew() {
	}

	public Crew(int id, String roomName, LocalDateTime createdAt, int memberCount) {
		super();
		this.id = id;
		this.roomName = roomName;
		this.createdAt = createdAt;
		this.memberCount = memberCount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	@Override
	public String toString() {
		return "ChatRoom [id=" + id + ", roomName=" + roomName + ", createdAt=" + createdAt + ", memberCount="
				+ memberCount + "]";
	}

}
