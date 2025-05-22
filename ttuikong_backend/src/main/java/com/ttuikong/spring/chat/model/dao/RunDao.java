package com.ttuikong.spring.chat.model.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RunDao {
    void updateImageUrl(@Param("userId") int userId,
                    @Param("startTime") LocalDateTime startTime,
                    @Param("imageUrl") String imageUrl);

    void insertRunningStatus(@Param("userId") int userId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("status") String status);
                             
    void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance, long duration);

    void updateDailyDuration(@Param("userId") int userId,
                         @Param("routeId") int routeId,
                         @Param("distance") double distance,
                         @Param("calories") double calories,
                         @Param("mood") String mood);

    List<Map<String, Object>> selectTop10UsersByDuration();

    List<Map<String, Object>> selectCrewMemberRecords(int crewId);

    Map<String, Object> selectCrewGoalStats(int crewId);

     Map<String, Object> selectMyRanking(int userId);
}
