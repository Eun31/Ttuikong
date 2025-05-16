package com.ttuikong.spring.chat.model.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ttuikong.spring.chat.model.dao.RunDao;

@Service
public class RunServiceImpl implements RunService {

    @Autowired
    private RunDao runDao;

    @Override
    public void updateImageUrl(int userId, String startTime, String imageUrl) {
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
}
