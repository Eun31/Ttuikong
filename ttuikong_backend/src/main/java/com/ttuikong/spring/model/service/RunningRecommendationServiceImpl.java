package com.ttuikong.spring.model.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ttuikong.spring.model.dao.RunningRecommendationDao;
import com.ttuikong.spring.model.dto.RunningRecommendation;

@Service
public class RunningRecommendationServiceImpl implements RunningRecommendationService{

	private final RunningRecommendationDao recommendationDao;
	
	@Autowired
	public RunningRecommendationServiceImpl(RunningRecommendationDao recommendationDao) {
		this.recommendationDao = recommendationDao;
	}
	
	@Override
	@Transactional
	public RunningRecommendation getLatestValidRecommendation(int userId) {
		LocalDateTime now = LocalDateTime.now();
		return recommendationDao.getLatestValidRecommendation(userId, now);
	}

	@Override
	@Transactional
	public int saveRecommendation(RunningRecommendation recommendation) {
		return recommendationDao.insertRecommendation(recommendation);
	}

	@Override
	@Transactional
	public boolean updateRecommendationFollowed(int recommendationId, boolean followed) {
		return recommendationDao.updateRecommendationFollowed(recommendationId, followed) > 0;
	}

	@Override
	@Transactional
	public void deleteExpiredRecommendations() {
		LocalDateTime now = LocalDateTime.now();
		recommendationDao.deleteExpiredRecomendations(now);
	}

	// 매일 새벽 3시에 만료된 추천 데이터 정리
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredRecommendations() {
        deleteExpiredRecommendations();
    }
}
