package com.ttuikong.spring.model.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ttuikong.spring.model.dto.Board;

public interface BoardService {
    // 전체 게시글 조회
    List<Board> getBoardList();
    
    // 게시글 상세 조회
    Board readBoard(int boardId);
    
    // 게시글 작성
    void saveBoard(Board board, MultipartFile image) throws IOException;
    
    // 게시글 수정
    boolean modifyBoard(Board board, MultipartFile image, boolean imageRemoved) throws IOException;
    
    // 게시글 삭제
    boolean removeBoard(int boardId);

}