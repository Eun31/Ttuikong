package com.ttuikong.spring.chat.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface RunService {
    public void updateImageUrl(int userId, LocalDateTime startTime, String imageUrl);

    public void insertRunningStatus(int userId, LocalDateTime startTime, String status);

    public void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance,
            long duration);

    public void updateDailyDuration(int userId, int routeId, double distance, double calories, String mood);

    public List<Map<String, Object>> getTop10UsersByDuration();

    public List<Map<String, Object>> getCrewMemberRecords(int crewId);

    public Map<String, Object> getCrewGoalStats(int crewId);

    public Map<String, Object> getMyRanking(int userId);
}
