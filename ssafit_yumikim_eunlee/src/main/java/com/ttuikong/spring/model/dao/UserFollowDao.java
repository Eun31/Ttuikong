package com.ttuikong.spring.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ttuikong.spring.model.dto.UserFollow;

@Mapper
public interface UserFollowDao {
	// 팔로우 추가
	void addFollow(UserFollow follow);

	// 팔로우 취소
	void removeFollow(int followerId, int followingId);

	// 특정 팔로우 조회
	UserFollow getFollow(int followerId, int followingId);

	// 팔로워 목록 조회
	List<UserFollow> getFollowers(int userId);

	// 팔로잉 목록 조회
	List<UserFollow> getFollowings(int userId);
}
