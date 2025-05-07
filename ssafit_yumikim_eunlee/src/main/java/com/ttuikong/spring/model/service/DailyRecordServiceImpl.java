package com.ttuikong.spring.model.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ttuikong.spring.model.dao.DailyRecordDao;
import com.ttuikong.spring.model.dto.DailyRecord;

@Service
public class DailyRecordServiceImpl implements DailyRecordService {

	private final DailyRecordDao dailyRecordDao;

	public DailyRecordServiceImpl(DailyRecordDao dailyRecordDao) {
		this.dailyRecordDao = dailyRecordDao;
	}

	@Override
	public void addRecord(DailyRecord record) {
	    Integer routeId = dailyRecordDao.findRouteIdByDate(record.getDate());
	    if (routeId != null) {
	        record.setRouteId(routeId);
	    } else {
	        record.setRouteId(null);
	    }
	    dailyRecordDao.addRecord(record);
	}


	@Override
	public List<DailyRecord> getRecordsByMonth(int userId, int year, int month) {
		return dailyRecordDao.getRecordsByMonth(userId, year, month);
	}

	@Override
	public List<DailyRecord> findByUserId(int userId) {
		return dailyRecordDao.findByUserId(userId);
	}
}
