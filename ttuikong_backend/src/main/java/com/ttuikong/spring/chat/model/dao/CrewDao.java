package com.ttuikong.spring.chat.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ttuikong.spring.chat.model.dto.Crew;

@Mapper
public interface CrewDao {
	boolean insertCrew(Crew crew);

	Crew selectCrewById(int crewId);

	boolean deleteCrewById(int crewId);

	List<Crew> selectAllCrews();

	boolean didMeetAnyCrewGoal(@Param("userId") int userId);
}
