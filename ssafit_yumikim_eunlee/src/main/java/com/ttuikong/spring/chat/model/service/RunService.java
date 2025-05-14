package com.ttuikong.spring.chat.model.service;

import java.time.LocalDateTime;

public interface RunService {
    public void updateImageUrl(int userId, String startTime, String imageUrl);

    void insertRunningStatus(int userId, LocalDateTime startTime, String status);

    void updateRunRecord(int userId, LocalDateTime startTime, LocalDateTime endTime, double distance, long duration);

}
