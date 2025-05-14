package com.ttuikong.spring.chat.model.dao;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RunDao {
    public void updateImageUrl(int userId, String startTime, String imageUrl);

    void insertRunningStatus(int userId, LocalDateTime startTime, String status);

    void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance, long duration);
}
