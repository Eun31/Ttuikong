package com.ttuikong.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.service.BoardService;

@RestController
@RequestMapping("/api/board")
public class BoardController {

	//생성자 주입
	private final BoardService boardService;
	
	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}
	
	//게시글 작성
	@PostMapping("/post")
	public ResponseEntity<Void> write(@RequestBody Board board){
		boardService.writeBoard(board);
		
		return new ResponseEntity<Void>(HttpStatus.CREATED);
	}
	
	//전체 게시글 조회
	@GetMapping("")
	public ResponseEntity<List<Board>> list(){
		List<Board> list = boardService.getBoardList();
		
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	//게시글 조회
	@GetMapping("/{postId}")
	public ResponseEntity<Board> detail(@PathVariable("postId") int postId){
		Board board = boardService.readBoard(postId);
		
		if(board != null) {
			return new ResponseEntity<>(board, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	//게시글 수정
	@PutMapping("/{postId}")
	public ResponseEntity<Void> update(@PathVariable("postId") int postId, @RequestBody Board board){
		board.setId(postId);
		boardService.modifyBoard(board);
		
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	//게시글 삭제
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> delete(@PathVariable("postId") int postId){
		boolean isDeleted = boardService.removeBoard(postId);
		
		if(isDeleted) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
		return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
	}
	
	//댓글 작성
//	@PostMapping("/{postId}/comment")
//	public ResponseEntity<>
	
}
