package com.ttuikong.spring.chat.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ttuikong.spring.chat.model.dao.CrewDao;
import com.ttuikong.spring.chat.model.dao.CrewMemberDao;
import com.ttuikong.spring.chat.model.dto.Crew;

@Service
public class CrewServiceImpl implements CrewService {

    @Autowired
    private CrewDao crewDao;

    @Autowired
    private CrewMemberDao crewMemberDao;

    @Override
    public boolean createCrew(Crew crew) {
        boolean result = crewDao.insertCrew(crew);
        if (result) {
        	crewMemberDao.insertCrewMember(crew.getId(), crew.getCreatorId()); // 생성자 자동가입
        }
        return result;
    }

    @Override
    public Crew getCrew(int crewId) {
        return crewDao.selectCrewById(crewId);
    }

    @Override
    public boolean deleteCrew(int crewId, int creatorId) {
        Crew crew = crewDao.selectCrewById(crewId);
        if (crew == null || crew.getCreatorId() != creatorId) {
            return false;
        }
        return crewDao.deleteCrewById(crewId);
    }

    @Override
    public List<Crew> getAllCrews() {
        return crewDao.selectAllCrews();
    }
}
