package com.ttuikong.spring.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ttuikong.spring.model.dao.BoardDao;
import com.ttuikong.spring.model.dto.Board;

@Service
public class BoardServiceImpl implements BoardService{

	private final BoardDao boardDao;
	
	public BoardServiceImpl(BoardDao boardDao) {
		this.boardDao = boardDao;
	}
	
	@Override
	public void writeBoard(Board board) {
		boardDao.insertBoard(board);
	}

	@Override
	public List<Board> getBoardList() {
		return boardDao.selectAll();
	}

	@Override
	public Board readBoard(int postId) {
		return boardDao.selectOne(postId);
	}

	@Override
	public void modifyBoard(Board board) {
		boardDao.updateBoard(board);
	}

	@Override
	public boolean removeBoard(int postId) {
		int result = boardDao.deleteBoard(postId);
		
		return result == 1;
	}

}
