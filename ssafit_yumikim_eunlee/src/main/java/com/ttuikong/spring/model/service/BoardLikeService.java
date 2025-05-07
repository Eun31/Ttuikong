package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.BoardLike;
import com.ttuikong.spring.model.dto.User;

public interface BoardLikeService {

	void toggleLike(int postId, int id);

	boolean findBoardLikeStatus(int postId, int id);

	int countBoardLikes(int postId);

	List<User> findBoardLikeUsers(int postId);

	List<BoardLike> findUserLikedBoards(int userId);

}
