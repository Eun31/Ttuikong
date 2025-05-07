package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.DailyRecord;

public interface DailyRecordService {
	// 사용자 운동 기록 추가
	void addRecord(DailyRecord record);

	// 사용자 운동 기록 조회 (월 단위)
	List<DailyRecord> getRecordsByMonth(int userId, int year, int month);
	
	// 사용자의 모든 운동 기록 조회
    List<DailyRecord> findByUserId(int userId);
}
