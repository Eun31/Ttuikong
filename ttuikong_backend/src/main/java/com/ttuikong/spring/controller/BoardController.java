package com.ttuikong.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/board")
@CrossOrigin(origins = "*")
public class BoardController {

	// 생성자 주입
	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	// 게시글 작성
	@PostMapping(value = "/post")
	@LoginRequired
	public ResponseEntity<?> writeBoard(@RequestPart Board board,
			@RequestPart(value = "image", required = false) MultipartFile image, @LoginUser User loginUser) {
		try {
			// 로그인한 사용자 ID 설정
			board.setUserId(loginUser.getId());

			boardService.saveBoard(board, image);
			return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 작성되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
	}

	// 전체 게시글 조회
	@GetMapping("")
	public ResponseEntity<?> list() {
		try {
			List<Board> list = boardService.getBoardList();

			if (list.isEmpty()) {
				return ResponseEntity.ok().body("게시글이 존재하지 않습니다.");
			}
			return ResponseEntity.ok(list);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 목록을 불러오는 중 오류가 발생했습니다.");
		}
	}

	// 특정 게시글 조회
	@GetMapping("/{postId}")
	public ResponseEntity<?> detail(@PathVariable("postId") int postId) {
		try {
			Board board = boardService.readBoard(postId);

			if (board != null)
				return ResponseEntity.ok(board);
			else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 게시글이 존재하지 않습니다.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글을 조회하는 중 오류가 발생했습니다.");
		}
	}

	// 사용자별 게시글 조회
	@GetMapping("/user/{userId}")
	@LoginRequired
	public ResponseEntity<?> getUserBoards(@PathVariable("userId") int userId) {
		try {
			List<Board> list = boardService.readBoardByUserId(userId);

			if (list.isEmpty()) {
				return ResponseEntity.ok().body("게시글이 존재하지 않습니다.");
			}
			return ResponseEntity.ok(list);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 목록을 불러오는 중 오류가 발생했습니다.");
		}
	}

	@PutMapping("/{postId}")
	@LoginRequired
	public ResponseEntity<?> updatePost(@PathVariable int postId, @RequestPart Board board, // 직접 Board 객체로 받기
			@RequestPart(required = false) MultipartFile image,
			@RequestParam(defaultValue = "false") boolean imageRemoved, @LoginUser User loginUser) {
		try {
			Board existingBoard = boardService.readBoard(postId);
			if (existingBoard == null)
				return ResponseEntity.notFound().build();
			if (existingBoard.getUserId() != loginUser.getId())
				return ResponseEntity.status(403).build();

			board.setPostId(postId);
			board.setUserId(loginUser.getId());

			boolean updated = boardService.modifyBoard(board, image, imageRemoved);
			return updated ? ResponseEntity.ok("수정 완료") : ResponseEntity.status(500).body("수정 실패");

		} catch (Exception e) {
			return ResponseEntity.badRequest().body("요청 처리 실패");
		}
	}

	// 게시글 삭제
	@DeleteMapping("/{postId}")
	@LoginRequired
	public ResponseEntity<?> delete(@PathVariable("postId") int postId, @LoginUser User loginUser) {
		try {
			Board existingBoard = boardService.readBoard(postId);

			if (existingBoard == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("삭제할 게시글이 존재하지 않습니다.");
			}

			// 게시글 작성자와 로그인 사용자가 일치하는지 확인
			if (existingBoard.getUserId() != loginUser.getId() && !"ADMIN".equals(loginUser.getRole())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("게시글 삭제 권한이 없습니다.");
			}
			boolean isDeleted = boardService.removeBoard(postId);

			if (isDeleted) {
				return ResponseEntity.ok().build();
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 삭제에 실패했습니다.");
			}
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 게시글 ID입니다: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글을 삭제하는 중 오류가 발생했습니다.");
		}
	}
	
	@Operation(summary = "인기 게시글 조회", description = "좋아요 수 기준으로 정렬된 인기 게시글을 조회합니다.")
	@GetMapping("/popular")
	public ResponseEntity<List<Board>> getPopularPosts(
	        @RequestParam(defaultValue = "10") int limit) {
	    List<Board> popularPosts = boardService.getPopularPosts(limit);
	    return ResponseEntity.ok(popularPosts);
	}

	@Operation(summary = "팔로우 사용자 게시글 조회", description = "현재 사용자가 팔로우한 사용자들의 게시글을 조회합니다.")
	@GetMapping("/following")
	@LoginRequired
	public ResponseEntity<List<Board>> getFollowingPosts(
	        @LoginUser User loginUser,
	        @RequestParam(defaultValue = "10") int limit) {
	    List<Board> followingPosts = boardService.getFollowingPosts(loginUser.getId(), limit);
	    return ResponseEntity.ok(followingPosts);
	}
	
}