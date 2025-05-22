package com.ttuikong.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.Board;
import com.ttuikong.spring.model.dto.Comment;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.BoardService;
import com.ttuikong.spring.model.service.CommentService;

@RestController
@RequestMapping("/api/board")
public class CommentController {

    private final BoardService boardService;
    private final CommentService commentService;
    
    public CommentController(BoardService boardService, CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }
    
    // 댓글 작성
    @PostMapping("/{postId}/comment")
    @LoginRequired
    public ResponseEntity<?> writeComment(@PathVariable("postId") int postId, 
                                          @RequestBody Comment comment,
                                          @LoginUser User loginUser) {
        try {
            Board existingBoard = boardService.readBoard(postId);
            
            if (existingBoard == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 게시글이 존재하지 않습니다.");
            }
            
            // 사용자 정보 설정
            comment.setUserId(loginUser.getId());
            comment.setBoardId(postId);
            
            commentService.writeComment(comment);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("댓글이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("댓글 작성 중 오류가 발생했습니다.");
        }
    }
    
    //댓글 전체 조회
    @GetMapping("/{postId}/comment")
    public ResponseEntity<?> commentList(@PathVariable("postId") int postId) {
        try {
            Board existingBoard = boardService.readBoard(postId);
            
            if (existingBoard == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 게시글이 존재하지 않습니다.");
            }
            
            List<Comment> commentList = commentService.getCommentList(postId);
            
            return ResponseEntity.ok(commentList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("댓글 목록을 조회하는 중 오류가 발생했습니다.");
        }
    }
    
    //댓글 수정
    @PutMapping("/{postId}/comment/{commentId}")
    @LoginRequired
    public ResponseEntity<?> updateComment(@PathVariable("postId") int postId, 
                                           @PathVariable("commentId") int commentId, 
                                           @RequestBody Comment comment,
                                           @LoginUser User loginUser) {
        try {
            Board existingBoard = boardService.readBoard(postId);
            
            if (existingBoard == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 게시글이 존재하지 않습니다.");
            }
            
            Comment existingComment = commentService.getComment(commentId);
            
            if (existingComment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("수정할 댓글이 존재하지 않습니다.");
            }
            
            if (existingComment.getBoardId() != postId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("해당 게시글의 댓글이 아닙니다.");
            }
            
            // 댓글 작성자와 로그인 사용자가 일치하는지 확인
            if(existingComment.getUserId() != loginUser.getId() && !"ADMIN".equals(loginUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("댓글 수정 권한이 없습니다.");
            }
            
            comment.setId(commentId);
            comment.setUserId(loginUser.getId());
            comment.setBoardId(postId);
            
            commentService.modifyComment(comment);
            
            return ResponseEntity.ok().body("댓글이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("댓글을 수정하는 중 오류가 발생했습니다.");
        }
    }
    
    //댓글 삭제
    @DeleteMapping("/{postId}/comment/{commentId}")
    @LoginRequired
    public ResponseEntity<?> deleteComment(@PathVariable("postId") int postId, 
                                          @PathVariable("commentId") int commentId,
                                          @LoginUser User loginUser) {
        try {
            Board existingBoard = boardService.readBoard(postId);
            
            if (existingBoard == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 게시글이 존재하지 않습니다.");
            }
            
            // 댓글 존재 여부 확인
            Comment existingComment = commentService.getComment(commentId);
            
            if (existingComment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("삭제할 댓글이 존재하지 않습니다.");
            }
            
            // 댓글이 해당 게시글에 속하는지 확인
            if (existingComment.getBoardId() != postId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("해당 게시글의 댓글이 아닙니다.");
            }
            
            // 댓글 작성자와 로그인 사용자가 일치하는지 확인
            if(existingComment.getUserId() != loginUser.getId() && !"ADMIN".equals(loginUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("댓글 삭제 권한이 없습니다.");
            }
            
            boolean isDeleted = commentService.removeComment(commentId);
            
            if (isDeleted) {
                return ResponseEntity.ok().body("댓글이 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 삭제에 실패했습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("댓글을 삭제하는 중 오류가 발생했습니다.");
        }
    }
}