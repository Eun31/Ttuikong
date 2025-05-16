package com.ttuikong.spring.chat.model.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RunDao {
    public void updateImageUrl(int userId, String startTime, String imageUrl);

    void insertRunningStatus(int userId, LocalDateTime startTime, String status);

    void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance, long duration);

    void updateDailyDuration(int userId);

    List<Map<String, Object>> selectTop10UsersByDuration();

    List<Map<String, Object>> selectCrewMemberRecords(int crewId);

    Map<String, Object> selectCrewGoalStats(int crewId);
}
