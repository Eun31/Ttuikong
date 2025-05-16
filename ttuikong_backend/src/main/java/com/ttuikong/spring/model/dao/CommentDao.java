package com.ttuikong.spring.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ttuikong.spring.model.dto.Comment;

@Mapper
public interface CommentDao {
    // 댓글 작성
    public void insertComment(int boardId, Comment comment);

    // 댓글 조회
    public Comment selectComment(int commentId);
    
    // 게시글의 댓글 목록 조회
    public List<Comment> selectAllComments(int boardId);

    // 댓글 수정
    public void updateComment(int boardId, Comment comment);

    // 댓글 삭제
    public int deleteComment(int commentId);
}