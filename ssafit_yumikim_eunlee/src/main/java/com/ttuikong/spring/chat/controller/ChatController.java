package com.ttuikong.spring.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.chat.model.dto.Chat;
import com.ttuikong.spring.chat.model.service.ChatService;
import com.ttuikong.spring.model.dto.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	@Autowired
	private ChatService chatService;

	@PostMapping("/{crewId}")
	@Operation(summary = "채팅 메시지 전송")
	@LoginRequired
	public ResponseEntity<String> sendMessage(@PathVariable int crewId, @RequestBody Chat chat, @Parameter(hidden = true) @LoginUser User loginUser) {
		chat.setSenderId(loginUser.getId());
		chat.setCrewId(crewId);
		chatService.sendMessage(chat);
		return ResponseEntity.ok("채팅 전송 성공");
	}

	@GetMapping("/{crewId}")
	@Operation(summary = "채팅 내역 조회")
	@LoginRequired
	public ResponseEntity<List<Chat>> getChatHistory(@PathVariable int crewId) {
		return ResponseEntity.ok(chatService.getChatHistory(crewId));
	}
}