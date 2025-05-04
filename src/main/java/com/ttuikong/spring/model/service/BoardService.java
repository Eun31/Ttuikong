package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.Board;

public interface BoardService {

	void writeBoard(Board board);

	List<Board> getBoardList();

	Board readBoard(int postId);

	void modifyBoard(Board board);

	boolean removeBoard(int postId);


}
