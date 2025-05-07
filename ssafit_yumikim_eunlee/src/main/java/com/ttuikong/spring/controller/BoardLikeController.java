package com.ttuikong.spring.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.BoardLike;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.BoardLikeService;

@RestController
@RequestMapping("/api/board")
public class BoardLikeController {
	private final BoardLikeService boardLikeService;
	
	public BoardLikeController(BoardLikeService boardLikeService) {
		this.boardLikeService = boardLikeService; 
	}
	
	@PostMapping("/{postId}/like")
	@LoginRequired
	public ResponseEntity<Void> toggleLike(@PathVariable("postId") int postId, @LoginUser User loginUser){
		boardLikeService.toggleLike(postId, loginUser.getId());
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/{postId}/like/status")
	@LoginRequired
	public ResponseEntity<Boolean> getLikeStatus(@PathVariable("postId") int postId, @LoginUser User loginUser){
		boolean isLiked = boardLikeService.findBoardLikeStatus(postId, loginUser.getId());
		return ResponseEntity.ok(isLiked);
	}
	
	@GetMapping("/{postId}/like/count")
	public ResponseEntity<Integer> getBoardLikeCount(@PathVariable("postId") int postId){
		int count = boardLikeService.countBoardLikes(postId);
		return ResponseEntity.ok(count);
	}
	
	@GetMapping("/{postId}/like/users")
	public ResponseEntity<List<User>> getBoardLikeUsers(@PathVariable("postId") int postId){
		List<User> users = boardLikeService.findBoardLikeUsers(postId);
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/user/likes")
	@LoginRequired
    public ResponseEntity<List<BoardLike>> getCurrentUserLikedBoards(@LoginUser User loginUser) {
        List<BoardLike> likes = boardLikeService.findUserLikedBoards(loginUser.getId());
        return ResponseEntity.ok(likes);
    }
	
	@GetMapping("/user/{userId}/likes")
    public ResponseEntity<List<BoardLike>> getUserLikedBoards(@PathVariable int userId) {
        List<BoardLike> likes = boardLikeService.findUserLikedBoards(userId);
        return ResponseEntity.ok(likes);
    }
}
