package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.User;

public interface UserService {
	// 전체 사용자 목록 조회
	List<User> getUserList();

	// 사용자 등록
	void signup(User user);

	// 로그인
	User login(String email, String password);

	// 사용자 ID로 조회
	User getUserById(int id);

	// 이메일로 사용자 조회
	User getUserByEmail(String email);

	// 사용자 정보 수정
	void updateUser(User user);

	// 사용자 삭제
	void deleteUser(int id);
}
