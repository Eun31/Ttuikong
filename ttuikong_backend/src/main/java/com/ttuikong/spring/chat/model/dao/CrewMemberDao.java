package com.ttuikong.spring.chat.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ttuikong.spring.model.dto.User;

@Mapper
public interface CrewMemberDao {
	int insertCrewMember(int crewId, int userId);

	int deleteCrewMember(int crewId, int userId);

	List<Integer> getUserIdsByCrewId(int crewId);

	int countMembersInCrew(int crewId);
	
	List<User> selectMembersByCrewId(int crewId);

}
