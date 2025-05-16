package com.ttuikong.spring.model.service;

import com.ttuikong.spring.model.dto.RunningRecommendation;

public interface RunningRecommendationService {

	RunningRecommendation getLatestValidRecommendation(int id);

	int saveRecommendation(RunningRecommendation recommendation);
	
	boolean updateRecommendationFollowed(int recommendationId, boolean followed);
	
	void deleteExpiredRecommendations();



}
