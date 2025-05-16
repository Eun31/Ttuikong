package com.ttuikong.spring.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ttuikong.spring.model.dto.Board;

@Mapper
public interface BoardDao {
    // 게시글 등록
    public void insertBoard(Board board);

    // 전체 게시글 조회
    public List<Board> selectAll();

    // 상세 게시글 조회
    public Board selectById(int postId);

    // 게시글 수정
    public int updateBoard(Board board);

    // 게시글 삭제
    public int deleteBoard(int postId);
    
    // 사용자별 게시글 조회
    List<Board> selectByUserId(int userId);
    
    // 카테고리별 게시글 조회
    List<Board> selectByCategory(String category);
}