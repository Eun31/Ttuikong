package com.ttuikong.spring.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ttuikong.spring.model.dao.CommentDao;
import com.ttuikong.spring.model.dto.Comment;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    
    public CommentServiceImpl(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    @Override
    public void writeComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수 입력 항목입니다.");
        }
        
        commentDao.insertComment(comment.getBoardId(), comment);
    }

    @Override
    public Comment getComment(int commentId) {
        return commentDao.selectComment(commentId);
    }

    @Override
    public List<Comment> getCommentList(int postId) {
        return commentDao.selectAllComments(postId);
    }

    @Override
    public boolean modifyComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수 입력 항목입니다.");
        }
        
        commentDao.updateComment(comment.getBoardId(), comment);
        return true;
    }

    @Override
    public boolean removeComment(int commentId) {
        return commentDao.deleteComment(commentId) > 0;
    }
}