package com.ttuikong.spring.model.dao;

import java.util.List;

import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.dto.Comment;


public interface BoardDao {

	public void insertBoard(Board board);

	public List<Board> selectAll();

	public Board selectOne(int postId);

	public int updateBoard(Board board);

	public int deleteBoard(int postId);

	public void insertComment(int postId, Comment comment);

	public Comment selectComment(int commentId);
	
	public List<Comment> selectAllComments(int postId);

	public void updateComment(int postId, Comment comment);

	public int deleteComment(int commentId);

}
