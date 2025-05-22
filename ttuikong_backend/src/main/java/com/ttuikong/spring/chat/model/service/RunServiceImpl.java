package com.ttuikong.spring.chat.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ttuikong.spring.chat.model.dao.RunDao;

@Service
public class RunServiceImpl implements RunService {

    @Autowired
    private RunDao runDao;

    @Override
    public void updateImageUrl(int userId, LocalDateTime startTime, String imageUrl) {
        runDao.updateImageUrl(userId, startTime, imageUrl);
    }

    @Override
    public void insertRunningStatus(int userId, LocalDateTime startTime, String status) {
        runDao.insertRunningStatus(userId, startTime, status);
    }

    @Override
    public void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance,
            long duration) {
        runDao.updateRunRecord(userId, startTime, endTime, distance, duration);
    }

    @Override
    public void updateDailyDuration(int userId, int routeId, double distance, double calories, String mood) {
        runDao.updateDailyDuration(userId, routeId, distance, calories, mood);
    }


    @Override
    public List<Map<String, Object>> getTop10UsersByDuration() {
        return runDao.selectTop10UsersByDuration();
    }

    @Override
    public List<Map<String, Object>> getCrewMemberRecords(int crewId) {
        return runDao.selectCrewMemberRecords(crewId);
    }

    @Override
    public Map<String, Object> getCrewGoalStats(int crewId) {
        return runDao.selectCrewGoalStats(crewId);
    }

    @Override
    public Map<String, Object> getMyRanking(int userId) {
        return runDao.selectMyRanking(userId);
    }
}
