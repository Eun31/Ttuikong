package com.ttuikong.spring.chat.model.dto;

import java.time.LocalDateTime;

public class Chat {
	private int id;
	private int crewId;
	private int senderId;
	private String message;
	private LocalDateTime sentAt;

	public Chat() {
	}

	public Chat(int id, int crewId, int senderId, String message, LocalDateTime sentAt) {
		super();
		this.id = id;
		this.crewId = crewId;
		this.senderId = senderId;
		this.message = message;
		this.sentAt = sentAt;
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

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	@Override
	public String toString() {
		return "Chat [id=" + id + ", crewId=" + crewId + ", senderId=" + senderId + ", message=" + message + ", sentAt="
				+ sentAt + "]";
	}

}
