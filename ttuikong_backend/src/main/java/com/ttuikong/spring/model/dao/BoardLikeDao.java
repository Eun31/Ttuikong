package com.ttuikong.spring.model.dao;

import java.util.List;
import java.util.Optional;

import com.ttuikong.spring.model.dto.BoardLike;

public interface BoardLikeDao {
	
	Optional<BoardLike> selectByBoardIdAndUserId(int postId, int userId);

	void deleteById(int id);

	void insertBoardLike(BoardLike boardLike);

	int countByBoardId(int postId);

	List<Integer> selectUserIdsByBoardId(int postId);

	List<BoardLike> selectAllByUserId(int userId);


}
