package com.ttuikong.spring.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
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

		// 새 이미지가 업로드된 경우
		if (image != null && !image.isEmpty()) {
			if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
				fileStorageService.deleteFile(currentImageUrl);
			}

			String newImageUrl = fileStorageService.storeFile(image);
			board.setImageUrl(newImageUrl);
		}

		// 이미지 삭제가 요청된 경우
		else if (imageRemoved) {
			if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
				fileStorageService.deleteFile(currentImageUrl);
			}
		}

		// 이미지 URL 목록 초기화
		board.setImageUrl(null);
		return boardDao.updateBoard(board) > 0;
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
}