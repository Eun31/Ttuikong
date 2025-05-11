package com.ttuikong.spring.chat.model.service;

import com.ttuikong.spring.chat.model.dto.Crew;

public interface CrewService {
	public boolean createCrew(Crew crew);

	public Crew getCrew(int id);

	public boolean deleteCrew(int id, int creatorId);

	public Object getAllCrews();
}
