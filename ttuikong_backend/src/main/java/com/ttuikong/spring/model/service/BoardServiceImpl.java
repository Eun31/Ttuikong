package com.ttuikong.spring.model.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ttuikong.spring.model.dao.BoardDao;
import com.ttuikong.spring.model.dto.Board;

@Service
public class BoardServiceImpl implements BoardService {

	private final BoardDao boardDao;
	private final FileStorageService fileStorageService;

	public BoardServiceImpl(BoardDao boardDao, FileStorageService fileStorageService) {
		this.boardDao = boardDao;
		this.fileStorageService = fileStorageService;
	}

	@Override
	public List<Board> getBoardList() {
		return boardDao.selectAll();
	}

	@Override
	public Board readBoard(int boardId) {
	    return boardDao.selectById(boardId);
	}

	@Override
	public void saveBoard(Board board, MultipartFile image) throws IOException {
		if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
		}
		if (board.getContent() == null || board.getContent().trim().isEmpty()) {
			throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
		}
		if (board.getUserId() <= 0) {
			throw new IllegalArgumentException("유효한 사용자 정보가 필요합니다.");
		}

		// 이미지 처리
		if (image != null && !image.isEmpty()) {
			String imageUrl = fileStorageService.storeFile(image);
			board.setImageUrl(imageUrl);
		}

		boardDao.insertBoard(board);
	}

	@Override
	public boolean modifyBoard(Board board, MultipartFile image, boolean imageRemoved) throws IOException {
	    Board existingBoard = boardDao.selectById(board.getPostId());

	    if (existingBoard == null) {
	        throw new IllegalArgumentException("수정할 게시글이 존재하지 않습니다.");
	    }

	    // 입력값 검증
	    if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
	        throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
	    }
	    if (board.getContent() == null || board.getContent().trim().isEmpty()) {
	        throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
	    }

	    String currentImageUrl = existingBoard.getImageUrl();
	    
	    System.out.println("=== 이미지 처리 시작 ===");
	    System.out.println("게시글 ID: " + board.getPostId());
	    System.out.println("기존 이미지 URL: " + currentImageUrl);
	    System.out.println("새 이미지 있음: " + (image != null && !image.isEmpty()));
	    System.out.println("이미지 삭제 요청: " + imageRemoved);

	    // Step 1: 기존 이미지 삭제 (삭제 요청이 있거나 새 이미지가 있는 경우)
	    if ((imageRemoved || (image != null && !image.isEmpty())) && currentImageUrl != null && !currentImageUrl.isEmpty()) {
	        boolean deleted = fileStorageService.deleteFile(currentImageUrl);
	        System.out.println("기존 파일 삭제 결과: " + deleted);
	    }
	    
	    // Step 2: 새 이미지 설정
	    if (image != null && !image.isEmpty()) {
	        // 새 이미지 업로드
	        System.out.println("새 이미지 업로드 처리");
	        String newImageUrl = fileStorageService.storeFile(image);
	        board.setImageUrl(newImageUrl);
	        System.out.println("새 이미지 URL 설정: " + newImageUrl);
	    } else if (imageRemoved) {
	        // 삭제만 하고 새 이미지 없음
	        System.out.println("이미지 삭제만 처리");
	        board.setImageUrl(null);
	        System.out.println("board.getImageUrl() 설정 후: " + board.getImageUrl());
	    } else {
	        // 이미지 변경 없음
	        System.out.println("이미지 변경 없음 - 기존 URL 유지");
	        board.setImageUrl(currentImageUrl);
	    }
	    
	    System.out.println("최종 설정된 이미지 URL: " + board.getImageUrl());
	    
	    // 업데이트 전 Board 객체 전체 상태 확인
	    System.out.println("=== 업데이트 전 Board 객체 상태 ===");
	    System.out.println("postId: " + board.getPostId());
	    System.out.println("title: " + board.getTitle());
	    System.out.println("content: " + board.getContent());
	    System.out.println("category: " + board.getCategory());
	    System.out.println("imageUrl: " + board.getImageUrl());
	    System.out.println("userId: " + board.getUserId());
	    
	    int updateResult = boardDao.updateBoard(board);
	    System.out.println("DB 업데이트 결과: " + updateResult);
	    
	    // 업데이트 후 실제 DB 상태 확인
	    Board updatedBoard = boardDao.selectById(board.getPostId());
	    System.out.println("=== 업데이트 후 DB 상태 확인 ===");
	    System.out.println("DB의 실제 imageUrl: " + updatedBoard.getImageUrl());
	    System.out.println("DB의 title: " + updatedBoard.getTitle());
	    System.out.println("DB의 updated_at: " + updatedBoard.getUpdatedAt());
	    
	    return updateResult > 0;
	}
	
	@Override
	public boolean removeBoard(int boardId) {
		Board board = boardDao.selectById(boardId);

		if (board == null) {
			throw new IllegalArgumentException("유효한 게시글 ID가 필요합니다.");
		}

		if (board != null) {
			String imageUrl = board.getImageUrl();
			if (imageUrl != null && !imageUrl.isEmpty()) {
				fileStorageService.deleteFile(imageUrl);
			}
		}

		return boardDao.deleteBoard(boardId) > 0;
	}
	
	private boolean isImageFileExists(String imageUrl) {
	    if (imageUrl == null || imageUrl.isEmpty()) {
	        return false;
	    }
	    
	    try {
	        // "/uploads/" 부분 제거하고 실제 파일 경로 확인
	        if (imageUrl.startsWith("/uploads/")) {
	            String filename = imageUrl.substring("/uploads/".length());
	            Path filePath = Paths.get("src/main/resources/static/uploads").resolve(filename);
	            return Files.exists(filePath);
	        }
	        return false;
	    } catch (Exception e) {
	        return false;
	    }
	}
}