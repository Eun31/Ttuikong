package com.ttuikong.spring.chat.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.User;

public interface CrewMemberService {
	boolean joinCrew(int crewId, int userId);

	boolean leaveCrew(int crewId, int userId);
	
	List<User> getMembersByCrewId(int crewId);
}
