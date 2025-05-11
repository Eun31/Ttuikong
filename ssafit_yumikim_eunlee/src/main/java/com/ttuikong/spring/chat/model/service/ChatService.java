package com.ttuikong.spring.chat.model.service;

import java.util.List;

import com.ttuikong.spring.chat.model.dto.Chat;

public interface ChatService {
	public void sendMessage(Chat chat);

	public List<Chat> getChatHistory(int crewId);
}
