package com.ttuikong.spring.model.dao;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ttuikong.spring.model.dto.DailyRecord;

@Mapper
public interface DailyRecordDao {
    // 사용자 운동 기록 추가 
    void addRecord(DailyRecord record);

    // Route 테이블에서 날짜로 route_id 찾기
    Integer findRouteIdByDate(Date date);

    // 사용자 운동 기록 조회 (월 단위)
    List<DailyRecord> getRecordsByMonth(int userId, int year, int month);

    // 사용자의 모든 운동 기록 조회
    List<DailyRecord> findByUserId(int userId);
}
