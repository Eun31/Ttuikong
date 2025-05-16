package com.ttuikong.spring.model.service;

import java.util.List;

public interface UserFollowService {
	// 팔로우 추가
	void followUser(int followerId, int followingId);

	// 팔로우 취소
	void unfollowUser(int followerId, int followingId);

	// 특정 팔로우 조회
	boolean isFollowing(int followerId, int followingId);

	// 팔로워 목록 조회
	List<Integer> getFollowers(int userId);

	// 팔로잉 목록 조회
	List<Integer> getFollowings(int userId);
}
