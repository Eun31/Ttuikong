package com.ttuikong.spring.model.dto;

public class UserFollow {
	private int id;
	private int follower_id;
	private int following_id;
	private String created_at;

	public UserFollow() {
	}

	public UserFollow(int id, int follower_id, int following_id, String created_at) {
		super();
		this.id = id;
		this.follower_id = follower_id;
		this.following_id = following_id;
		this.created_at = created_at;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFollower_id() {
		return follower_id;
	}

	public void setFollower_id(int follower_id) {
		this.follower_id = follower_id;
	}

	public int getFollowing_id() {
		return following_id;
	}

	public void setFollowing_id(int following_id) {
		this.following_id = following_id;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	@Override
	public String toString() {
		return "UserFollow [id=" + id + ", follower_id=" + follower_id + ", following_id=" + following_id
				+ ", created_at=" + created_at + "]";
	}

}
