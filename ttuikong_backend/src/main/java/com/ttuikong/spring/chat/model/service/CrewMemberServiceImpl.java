package com.ttuikong.spring.chat.model.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ttuikong.spring.chat.model.dao.CrewDao;
import com.ttuikong.spring.chat.model.dao.CrewMemberDao;
import com.ttuikong.spring.chat.model.dto.Crew;
import com.ttuikong.spring.model.dto.User;

@Service
public class CrewMemberServiceImpl implements CrewMemberService {
    @Autowired
    private CrewDao crewDao;

    @Autowired
    private CrewMemberDao crewMemberDao;

    @Override
    public boolean joinCrew(int crewId, int userId) {
        Crew crew = crewDao.selectCrewById(crewId);

        // 챌린지 시작일이 오늘보다 이전이면 가입 불가
        if (crew.getStartDate() != null &&  LocalDate.now().isAfter(crew.getStartDate())) {
            return false;
        }

        // 인원 수 제한 (10명)
        int count = crewMemberDao.countMembersInCrew(crewId);
        if (count >= 10) {
            return false;
        }

        // 이미 가입한 유저인지 확인
        if (crewMemberDao.getUserIdsByCrewId(crewId).contains(userId)) {
            return false;
        }

        // 가입 처리
        crewMemberDao.insertCrewMember(crewId, userId);
        return true;
    }
    
    @Override
    public boolean leaveCrew(int crewId, int userId) {
        // 크루가 존재하는지, 가입 상태인지 등을 확인하고 삭제
        Crew crew = crewDao.selectCrewById(crewId);
        if (crew == null) return false;

        // 이미 가입하지 않았다면 탈퇴할 수 없음
        if (!crewMemberDao.getUserIdsByCrewId(crewId).contains(userId)) {
            return false;
        }

        return crewMemberDao.deleteCrewMember(crewId, userId) > 0;
    }

	@Override
	public List<User> getMembersByCrewId(int crewId) {
		return crewMemberDao.selectMembersByCrewId(crewId);
	}

}
