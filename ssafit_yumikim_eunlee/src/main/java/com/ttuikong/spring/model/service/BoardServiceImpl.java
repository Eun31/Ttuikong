package com.ttuikong.spring.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ttuikong.spring.model.dao.BoardDao;
import com.ttuikong.spring.model.dto.Board;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardDao boardDao;
    
    public BoardServiceImpl(BoardDao boardDao) {
        this.boardDao = boardDao;
    }
    
    @Override
    public List<Board> getBoardList() {
        return boardDao.selectAll();
    }

    @Override
    public Board readBoard(int boardId) {
        return boardDao.selectOne(boardId);
    }

    @Override
    public void writeBoard(Board board) {
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
        }
        if (board.getUserId() <= 0) {
            throw new IllegalArgumentException("유효한 사용자 정보가 필요합니다.");
        }
        
        boardDao.insertBoard(board);
    }

    @Override
    public boolean modifyBoard(Board board) {
        if (board.getId() <= 0) {
            throw new IllegalArgumentException("유효한 게시글 ID가 필요합니다.");
        }
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
        }
        
        return boardDao.updateBoard(board) > 0;
    }

    @Override
    public boolean removeBoard(int boardId) {
        if (boardId <= 0) {
            throw new IllegalArgumentException("유효한 게시글 ID가 필요합니다.");
        }
        
        return boardDao.deleteBoard(boardId) > 0;
    }
}