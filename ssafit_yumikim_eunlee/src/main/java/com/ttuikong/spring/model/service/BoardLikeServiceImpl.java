package com.ttuikong.spring.model.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ttuikong.spring.model.dao.BoardDao;
import com.ttuikong.spring.model.dao.BoardLikeDao;
import com.ttuikong.spring.model.dao.UserDao;
import com.ttuikong.spring.model.dto.BoardLike;
import com.ttuikong.spring.model.dto.User;

@Service
public class BoardLikeServiceImpl implements BoardLikeService{

	private final BoardLikeDao boardLikeDao;
	private final BoardDao boardDao;
	private final UserDao userDao;
	
	 public BoardLikeServiceImpl(BoardLikeDao boardLikeDao, BoardDao boardDao, UserDao userDao) {
		this.boardLikeDao = boardLikeDao;
		this.boardDao = boardDao;
		this.userDao = userDao;
	}
	
	 @Override
	 @Transactional
	 public void toggleLike(int postId, int id) {
		 //게시글 존재 여부 확인
		 if(boardDao.selectById(postId) == null) {
			 throw new RuntimeException("게시글을 찾을 수 없습니다.");
		 }
		 
		 //사용자 존재 여부 확인
		 if(userDao.selectById(id) == null) {
			 throw new RuntimeException("사용자를 찾을 수 없습니다.");
		 }
		 
		 Optional<BoardLike> existingLike = boardLikeDao.selectByBoardIdAndUserId(postId, id);
		 
		 if(existingLike.isPresent()) {
			 boardLikeDao.deleteById(existingLike.get().getId());
		 }
		 
		 else {
			 BoardLike boardLike = new BoardLike(0, id, postId, LocalDateTime.now());
			 boardLikeDao.insertBoardLike(boardLike);
		 }
	 }
	 
	@Override
	@Transactional(readOnly = true)
	public boolean findBoardLikeStatus(int postId, int id) {
		if(boardDao.selectById(postId) == null) {
			throw new RuntimeException("게시글을 찾을 수 없습니다.");
		}
		
		if(userDao.selectById(id) == null) {
			throw new RuntimeException("사용자를 찾을 수 없습니다.");
		}
		
		return boardLikeDao.selectByBoardIdAndUserId(postId, id).isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public int countBoardLikes(int postId) {
		if(boardDao.selectById(postId) == null) {
			throw new RuntimeException("게시글을 찾을 수 없습니다.");
		}
		return boardLikeDao.countByBoardId(postId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findBoardLikeUsers(int postId) {
		if(boardDao.selectById(postId) == null) {
			throw new RuntimeException("게시글을 찾을 수 없습니다.");
		}
		
		List<Integer> userIds = boardLikeDao.selectUserIdsByBoardId(postId);
		
		List<User> users = new ArrayList<>();
		for(Integer userId : userIds) {
			User user = userDao.selectById(userId);
			if(user != null) {
				users.add(user);
			}
		}
		return users;
	}

	@Override
	public List<BoardLike> findUserLikedBoards(int userId) {
		
		if(userDao.selectById(userId) == null) {
			throw new RuntimeException("사용자를 찾을 수 없습니다.");
		}
		return boardLikeDao.selectAllByUserId(userId);
	}


}
