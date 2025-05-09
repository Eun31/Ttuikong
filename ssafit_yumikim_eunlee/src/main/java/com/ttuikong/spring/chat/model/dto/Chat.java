package com.ttuikong.spring.chat.model.dto;

import java.time.LocalDateTime;

public class Chat {
	private int id;
	private int chatRoomId;
	private int senderId;
	private String message;
	private LocalDateTime sentAt;

	public Chat() {
	}

	public Chat(int id, int chatRoomId, int senderId, String message, LocalDateTime sentAt) {
		super();
		this.id = id;
		this.chatRoomId = chatRoomId;
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

	public int getChatRoomId() {
		return chatRoomId;
	}

	public void setChatRoomId(int chatRoomId) {
		this.chatRoomId = chatRoomId;
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
		return "ChatMessage [id=" + id + ", chatRoomId=" + chatRoomId + ", senderId=" + senderId + ", message="
				+ message + ", sentAt=" + sentAt + "]";
	}

}
