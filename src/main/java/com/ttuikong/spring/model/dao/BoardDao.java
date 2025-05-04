package com.ttuikong.spring.model.dao;

import java.util.List;

import com.ttuikong.spring.model.dto.Board;


public interface BoardDao {

	public void insertBoard(Board board);

	public List<Board> selectAll();

	public Board selectOne(int postId);

	public void updateBoard(Board board);

	public int deleteBoard(int postId);

}
