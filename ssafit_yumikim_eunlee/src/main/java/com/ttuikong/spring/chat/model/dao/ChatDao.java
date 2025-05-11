package com.ttuikong.spring.chat.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ttuikong.spring.chat.model.dto.Chat;

@Mapper
public interface ChatDao {
	void insertChat(Chat chat);

	List<Chat> selectChatsByCrewId(int crewId);
}
