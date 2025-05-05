package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.dto.Comment;

public interface BoardService {

	void writeBoard(Board board);

	List<Board> getBoardList();

	Board readBoard(int postId);

	boolean modifyBoard(Board board);

	boolean removeBoard(int postId);

	void writeComment(int postId, Comment comment);

	List<Comment> getCommentList(int postId);

	Comment getComment(int commentId);
	
	void modifyComment(int postId, Comment comment);

	boolean removeComment(int commentId);

}
