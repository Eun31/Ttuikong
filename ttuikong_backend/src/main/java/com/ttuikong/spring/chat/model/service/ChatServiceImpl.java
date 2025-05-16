package com.ttuikong.spring.chat.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ttuikong.spring.chat.model.dao.ChatDao;
import com.ttuikong.spring.chat.model.dao.CrewMemberDao;
import com.ttuikong.spring.chat.model.dto.Chat;

@Service
public class ChatServiceImpl implements ChatService {
	@Autowired
	private ChatDao chatDao;

	@Autowired
	private CrewMemberDao crewMemberDao;

	@Override
	public void sendMessage(Chat chat) {
		// crewId의 멤버인지 확인
		List<Integer> members = crewMemberDao.getUserIdsByCrewId(chat.getCrewId());
		if (!members.contains(chat.getSenderId())) {
			throw new IllegalArgumentException("해당 유저는 이 크루의 멤버가 아닙니다.");
		}

		chatDao.insertChat(chat);
	}

	@Override
	public List<Chat> getChatHistory(int crewId) {
		return chatDao.selectChatsByCrewId(crewId);
	}
}
