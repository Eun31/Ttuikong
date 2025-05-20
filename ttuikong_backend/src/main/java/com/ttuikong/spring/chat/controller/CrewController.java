package com.ttuikong.spring.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.chat.model.dto.Crew;
import com.ttuikong.spring.chat.model.service.CrewMemberService;
import com.ttuikong.spring.chat.model.service.CrewService;
import com.ttuikong.spring.model.dto.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/crew")
public class CrewController {

	@Autowired
	private CrewService crewService;
	
	@Autowired
	private CrewMemberService crewMemberService;

	/*
	 * 크루 생성, 크루 삭제, 크루 조회, 모든 크루 조회
	 */
	@Operation(summary = "크루 생성")
	@PostMapping("")
	@LoginRequired
	public ResponseEntity<String> createCrew(@RequestBody Crew crew, @Parameter(hidden = true) @LoginUser User loginUser) {
		crew.setCreatorId(loginUser.getId()); 
		boolean result = crewService.createCrew(crew);
		if (result) {
			return ResponseEntity.ok("크루 생성 완료");
		} else {
			return ResponseEntity.badRequest().body("크루 인원 초과 또는 생성 실패");
		}
	}

	@Operation(summary = "크루 조회")
	@GetMapping("/{crewId}")
	@LoginRequired
	public ResponseEntity<Crew> getCrew(@PathVariable int crewId) {
		Crew crew = crewService.getCrew(crewId);
		if (crew != null) {
			return ResponseEntity.ok(crew);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "크루 삭제")
	@DeleteMapping("/{crewId}")
	@LoginRequired
	public ResponseEntity<String> deleteCrew(@PathVariable int crewId, @RequestParam int creatorId) {
		boolean deleted = crewService.deleteCrew(crewId, creatorId);
		if (deleted) {
			return ResponseEntity.ok("크루 삭제 완료");
		} else {
			return ResponseEntity.badRequest().body("삭제 권한이 없거나 크루가 존재하지 않습니다.");
		}
	}

	@Operation(summary = "전체 그룹 리스트 조회")
	@GetMapping("")
	@LoginRequired
	public ResponseEntity<?> getAllCrews() {
		return ResponseEntity.ok(crewService.getAllCrews());
	}
	
	/*
	 * 크루 가입, 크루 탈퇴
	 */
	@Operation(summary = "크루 가입")
	@PostMapping("/{crewId}/join")
	@LoginRequired
	public ResponseEntity<String> joinCrew(@PathVariable int crewId, @RequestParam int userId) {
	    boolean result = crewMemberService.joinCrew(crewId, userId);
	    if (result) {
	        return ResponseEntity.ok("크루 가입 성공");
	    } else {
	        return ResponseEntity.badRequest().body("크루 가입 실패: 인원 초과, 중복 가입 또는 챌린지 시작됨");
	    }
	}
	
	@Operation(summary = "크루 탈퇴")
	@DeleteMapping("/{crewId}/leave")
	@LoginRequired
	public ResponseEntity<String> leaveCrew(@PathVariable int crewId, @RequestParam int userId) {
	    boolean result = crewMemberService.leaveCrew(crewId, userId);
	    if (result) {
	        return ResponseEntity.ok("크루 탈퇴 완료");
	    } else {
	        return ResponseEntity.badRequest().body("탈퇴 실패: 크루가 존재하지 않거나 가입되어 있지 않음");
	    }
	}
	
	/*
	 * 크루 멤버 목록 조회
	 */
	@Operation(summary = "크루 멤버 목록 조회")
	@GetMapping("/{crewId}/members")
	@LoginRequired
	public ResponseEntity<?> getCrewMembers(@PathVariable int crewId) {
	    List<User> members = crewMemberService.getMembersByCrewId(crewId);
	    return ResponseEntity.ok(members);
	}


}