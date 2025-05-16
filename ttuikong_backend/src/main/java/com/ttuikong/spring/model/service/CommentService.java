package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.Comment;

public interface CommentService {

	void writeComment(Comment comment);

	List<Comment> getCommentList(int postId);

	Comment getComment(int commentId);

	boolean modifyComment(Comment comment);

	boolean removeComment(int commentId);

}
