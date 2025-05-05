package com.ttuikong.spring.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ttuikong.spring.model.dao.BoardDao;
import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.dto.Comment;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardDao boardDao;
    
    public BoardServiceImpl(BoardDao boardDao) {
        this.boardDao = boardDao;
    }
    
    @Override
    public void writeBoard(Board board) {
        // 유효성 검증
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 항목입니다.");
        }
        
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 항목입니다.");
        }
        
        if (board.getTitle().length() > 200) {
            throw new IllegalArgumentException("제목은 200자 이내로 작성해주세요.");
        }
        
        if (board.getUserId() <= 0) {
            throw new IllegalArgumentException("유효한 사용자 정보가 필요합니다.");
        }
        
        boardDao.insertBoard(board);
    }

    @Override
    public List<Board> getBoardList() {
        List<Board> boardList = boardDao.selectAll();
        return boardList;
    }

    @Override
    public Board readBoard(int postId) {
        if (postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        Board board = boardDao.selectOne(postId);
        return board;
    }

    @Override
    public boolean modifyBoard(Board board) {
        // 유효성 검증
        if (board.getId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 항목입니다.");
        }
        
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 항목입니다.");
        }
        
        if (board.getTitle().length() > 200) {
            throw new IllegalArgumentException("제목은 200자 이내로 작성해주세요.");
        }
        
        // 게시글 존재 여부 확인
        Board existingBoard = boardDao.selectOne(board.getId());
        if (existingBoard == null) {
            throw new IllegalArgumentException("수정할 게시글이 존재하지 않습니다.");
        }
        
        int result = boardDao.updateBoard(board);
        return result == 1;
    }

    @Override
    public boolean removeBoard(int postId) {
        if (postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        // 게시글 존재 여부 확인
        Board existingBoard = boardDao.selectOne(postId);
        if (existingBoard == null) {
            throw new IllegalArgumentException("삭제할 게시글이 존재하지 않습니다.");
        }
        
        int result = boardDao.deleteBoard(postId);
        return result == 1;
    }

    @Override
    public void writeComment(int postId, Comment comment) {
        if (postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        // 게시글 존재 여부 확인
        Board existingBoard = boardDao.selectOne(postId);
        if (existingBoard == null) {
            throw new IllegalArgumentException("댓글을 작성할 게시글이 존재하지 않습니다.");
        }
        
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수 항목입니다.");
        }
        
        if (comment.getUserId() <= 0) {
            throw new IllegalArgumentException("유효한 사용자 정보가 필요합니다.");
        }
        
        boardDao.insertComment(postId, comment);
    }

    @Override
    public Comment getComment(int commentId) {
        if (commentId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 댓글 ID입니다.");
        }
        
        Comment comment = boardDao.selectComment(commentId);
        return comment;
    }
    
    @Override
    public List<Comment> getCommentList(int postId) {
        if (postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        // 게시글 존재 여부 확인
        Board existingBoard = boardDao.selectOne(postId);
        if (existingBoard == null) {
            throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
        }
        
        List<Comment> commentList = boardDao.selectAllComments(postId);
        return commentList;
    }

    @Override
    public void modifyComment(int postId, Comment comment) {
        if (postId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
        }
        
        if (comment.getId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 댓글 ID입니다.");
        }
        
        // 게시글 존재 여부 확인
        Board existingBoard = boardDao.selectOne(postId);
        if (existingBoard == null) {
            throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
        }
        
        // 댓글 존재 여부 확인
        Comment existingComment = boardDao.selectComment(comment.getId());
        if (existingComment == null) {
            throw new IllegalArgumentException("수정할 댓글이 존재하지 않습니다.");
        }
        
        // 댓글이 해당 게시글에 속하는지 확인
//        if (existingComment.getBoardId() != postId) {
//            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
//        }
        
        if (existingComment.getBoardId() != postId) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다. (댓글 boardId: " + 
                existingComment.getBoardId() + ", 요청 postId: " + postId + ")");
        }
        
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수 항목입니다.");
        }
        
        boardDao.updateComment(postId, comment);
    }

    @Override
    public boolean removeComment(int commentId) {
        if (commentId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 댓글 ID입니다.");
        }
        
        // 댓글 존재 여부 확인
        Comment existingComment = boardDao.selectComment(commentId);
        if (existingComment == null) {
            throw new IllegalArgumentException("삭제할 댓글이 존재하지 않습니다.");
        }
        
        int result = boardDao.deleteComment(commentId);
        return result == 1;
    }
}