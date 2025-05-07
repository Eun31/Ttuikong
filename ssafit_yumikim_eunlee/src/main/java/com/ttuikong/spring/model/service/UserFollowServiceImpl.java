package com.ttuikong.spring.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ttuikong.spring.model.dao.UserFollowDao;
import com.ttuikong.spring.model.dto.UserFollow;

@Service
public class UserFollowServiceImpl implements UserFollowService {

	private final UserFollowDao userFollowDao;

	public UserFollowServiceImpl(UserFollowDao userFollowDao) {
		this.userFollowDao = userFollowDao;
	}

	@Override
	public void followUser(int followerId, int followingId) {
		if (followerId == followingId) {
			throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
		}

		if (isFollowing(followerId, followingId)) {
			throw new IllegalStateException("이미 팔로우 중입니다.");
		}

		UserFollow follow = new UserFollow();
		follow.setFollower_id(followerId);
		follow.setFollowing_id(followingId);

		userFollowDao.addFollow(follow);
	}

	@Override
	public void unfollowUser(int followerId, int followingId) {
		userFollowDao.removeFollow(followerId, followingId);
	}

	@Override
	public boolean isFollowing(int followerId, int followingId) {
		UserFollow follow = userFollowDao.getFollow(followerId, followingId);
		return follow != null;
	}

	@Override
	public List<Integer> getFollowers(int userId) {
		List<UserFollow> followers = userFollowDao.getFollowers(userId);
		List<Integer> followerIds = new ArrayList<>();

		for (UserFollow follower : followers) {
			followerIds.add(follower.getFollower_id());
		}

		return followerIds;
	}

	@Override
	public List<Integer> getFollowings(int userId) {
		List<UserFollow> followings = userFollowDao.getFollowings(userId);
		List<Integer> followingIds = new ArrayList<>();

		for (UserFollow following : followings) {
			followingIds.add(following.getFollowing_id());
		}

		return followingIds;
	}

}
