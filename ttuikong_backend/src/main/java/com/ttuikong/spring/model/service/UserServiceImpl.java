package com.ttuikong.spring.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ttuikong.spring.model.dao.DailyRecordDao;
import com.ttuikong.spring.model.dao.UserDao;
import com.ttuikong.spring.model.dto.User;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private DailyRecordDao dailyRecordDao;
	
	@Override
	public List<User> getUserList() {
		return userDao.selectAll();
	}

	@Transactional
	@Override
	public void signup(User user) {
		userDao.insertUser(user);
	}

	@Override
	public User login(String email, String password) {
		Map<String, String> info = new HashMap<>();
		info.put("email", email);
		info.put("password", password);
		return userDao.selectOne(info);
	}

	@Override
	public User getUserById(int id) {
		return userDao.selectById(id);
	}

	@Override
	public User getUserByEmail(String email) {
		return userDao.selectByEmail(email);
	}

	@Transactional
	@Override
	public void updateUser(User user) {
		userDao.updateUser(user);
	}

	@Transactional
	@Override
	public void deleteUser(int id) {
		userDao.deleteUser(id);
	}

	@Override
	public void calculateAverageDistance(int id) {
		User user = userDao.selectById(id);
		int recordCount = dailyRecordDao.countByUserId(id);
		
		if(recordCount > 0 && user != null) {
			float avgDistance = user.getTotalDistance() / recordCount;
			user.setAvgDistance(avgDistance);
			userDao.updateUser(user);
		}
	}

	@Override
	public boolean checkPassword(int userId, String inputPassword) {
	    String dbPassword = userDao.getPasswordByUserId(userId);
	    return inputPassword.equals(dbPassword);
	}
	
	

}
