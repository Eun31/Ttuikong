package com.ttuikong.spring.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ttuikong.spring.model.dto.User;

@Mapper
public interface UserDao {
    // 전체 사용자 목록 조회
    List<User> selectAll();

    // 사용자 등록
    void insertUser(User user);

    // 로그인을 위한 이메일, 비밀번호로 사용자 조회
    User selectOne(String email);
    
    // 사용자 ID로 조회
    User selectById(int id);
    
    // 이메일로 사용자 조회
    User selectByEmail(String email);
    
    // 사용자 정보 수정
    void updateUser(User user);

    void updateActivityLevel(@Param("id") int id, @Param("activityLevel") String activityLevel);
    
    // 사용자 삭제
    void deleteUser(int id);

    //비밀번호 조회
	String getPasswordByUserId(long id);
}
