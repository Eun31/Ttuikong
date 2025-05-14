package com.ttuikong.spring.model.dao;

import java.time.LocalDateTime;

import com.ttuikong.spring.model.dto.RunningRecommendation;

public interface RunningRecommendationDao {

	int insertRecommendation(RunningRecommendation recommendation);

	RunningRecommendation getLatestValidRecommendation(int userId, LocalDateTime now);

	int updateRecommendationFollowed(int recommendationId, boolean followed);

	void deleteExpiredRecomendations(LocalDateTime now);

}
