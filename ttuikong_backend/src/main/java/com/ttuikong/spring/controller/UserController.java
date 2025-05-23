package com.ttuikong.spring.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.DailyRecord;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.DailyRecordService;
import com.ttuikong.spring.model.service.UserFollowService;
import com.ttuikong.spring.model.service.UserService;
import com.ttuikong.spring.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private UserFollowService userFollowService;
	@Autowired
	private DailyRecordService dailyRecordService;

	@Autowired
	private JwtUtil jwtUtil;

	// ADMIN -> 모든 사용자 목록 조회 허용
	@Operation(summary = "모든 사용자 목록 조회", description = "관리자가 모든 사용자 정보를 조회합니다.")
	@GetMapping
	@LoginRequired
	public ResponseEntity<List<User>> getAllUsers(@Parameter(hidden = true) @LoginUser User loginUser) {
		if (loginUser == null || !"ADMIN".equalsIgnoreCase(loginUser.getRole())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		List<User> users = userService.getUserList();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@Operation(summary = "현재 로그인한 사용자 조회", description = "현재 로그인 중인 사용자 정보를 조회합니다.")
	@GetMapping("/me")
	@LoginRequired
	public ResponseEntity<Map<String, Object>> getCurrentUser(@Parameter(hidden = true) @LoginUser User loginUser) {
		Map<String, Object> response = new HashMap<>();
		response.put("user", loginUser);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	 * 회원가입, 로그인, 로그아웃, 회원 탈퇴
	 */
	@Operation(summary = "회원가입", description = "신규 사용자를 등록합니다.")
	@PostMapping("/signup")
	public ResponseEntity<Map<String, Object>> signup(@RequestBody User user) {
		User existingUser = userService.getUserByEmail(user.getEmail());
		if (existingUser != null) {
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Email already exists");
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		}
		// role 기본값 지정
		if (user.getRole() == null || user.getRole().isBlank()) {
			user.setRole("USER");
		}
		userService.signup(user);
		Map<String, Object> response = new HashMap<>();
		response.put("message", "User created successfully");
		response.put("userId", user.getId());
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody User user, HttpServletResponse httpResponse) {
		User loggedUser = userService.login(user.getEmail(), user.getPassword());
		Map<String, Object> response = new HashMap<>();
		if (loggedUser == null) {
			response.put("message", "Invalid credentials");
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}
		String token = jwtUtil.generateToken(loggedUser);
		response.put("message", "Login successful");
		response.put("user", loggedUser);
		response.put("token", token);
		return ResponseEntity.ok(Map.of(
        "message", "Login successful",
        "user", loggedUser,
        "token", token 
    ));
	}

	@Operation(summary = "로그아웃", description = "사용자가 로그아웃합니다.")
	@PostMapping("/logout")
	@LoginRequired
	public ResponseEntity<Map<String, Object>> logout() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "Logout successful");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Operation(summary = "회원 탈퇴", description = "사용자가 자신의 계정을 삭제합니다.")
	@DeleteMapping("/{userId}")
	@LoginRequired
	public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int userId, @LoginUser User loginUser) {
		Map<String, Object> response = new HashMap<>();
		if (loginUser.getId() != userId) {
			response.put("message", "Forbidden");
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}
		userService.deleteUser(userId);
		response.put("message", "User deleted successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	 * 프로필 조회, 프로필 수정
	 */
	@Operation(summary = "프로필 조회", description = "사용자 ID로 특정 사용자 정보를 조회합니다.")
	@GetMapping("/{userId}/profile")
	@LoginRequired
	public ResponseEntity<User> getUserById(@PathVariable int userId, @LoginUser User loginUser) {
		if (loginUser.getId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		User user = userService.getUserById(userId);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "프로필 수정", description = "사용자가 자신의 정보를 수정합니다.")
	@PutMapping("/{userId}/profile")
	@LoginRequired
	public ResponseEntity<Map<String, Object>> updateUserProfile(@PathVariable int userId, @RequestBody User user,
			@LoginUser User loginUser) {
		Map<String, Object> response = new HashMap<>();
		if (loginUser.getId() != userId) {
			response.put("message", "Forbidden");
			return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
		}
		user.setId(userId);
		userService.updateUser(user);
		User updatedUser = userService.getUserById(userId);
		String newToken = jwtUtil.generateToken(updatedUser);
		response.put("message", "User updated successfully");
		response.put("token", newToken);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	 * 팔로잉 목록 조회, 팔로우 목록 조회, 팔로우 추가, 팔로우 삭제
	 */
	@Operation(summary = "팔로잉 목록 조회", description = "사용자 ID로 팔로우한 사용자 목록을 조회합니다.")
	@GetMapping("/{userId}/followings")
	@LoginRequired
	public ResponseEntity<List<Integer>> getFollowingById(@PathVariable int userId,
			@Parameter(hidden = true) @LoginUser User loginUser) {
		if (loginUser.getId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (userService.getUserById(userId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		List<Integer> followings = userFollowService.getFollowings(userId);
		return new ResponseEntity<>(followings, HttpStatus.OK);
	}

	@Operation(summary = "팔로워 목록 조회", description = "사용자 ID로 팔로워 목록을 조회합니다.")
	@GetMapping("/{userId}/followers")
	@LoginRequired
	public ResponseEntity<List<Integer>> getFollowersById(@PathVariable int userId,
			@Parameter(hidden = true) @LoginUser User loginUser) {
		if (loginUser.getId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (userService.getUserById(userId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		List<Integer> followers = userFollowService.getFollowers(userId);
		return new ResponseEntity<>(followers, HttpStatus.OK);
	}

	@Operation(summary = "팔로우 추가", description = "특정 사용자를 팔로우합니다.")
	@PostMapping("/{userId}/follow/{targetId}")
	@LoginRequired
	public ResponseEntity<Void> followUser(@PathVariable int userId, @PathVariable int targetId,
			@Parameter(hidden = true) @LoginUser User loginUser) {
		if (loginUser.getId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (userService.getUserById(userId) == null || userService.getUserById(targetId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			userFollowService.followUser(userId, targetId);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (IllegalStateException e) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}

	@Operation(summary = "팔로우 취소", description = "특정 사용자에 대한 팔로우를 취소합니다.")
	@DeleteMapping("/{userId}/follow/{targetId}")
	@LoginRequired
	public ResponseEntity<Void> unfollowUser(@PathVariable int userId, @PathVariable int targetId,
			@Parameter(hidden = true) @LoginUser User loginUser) {
		if (loginUser.getId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (userService.getUserById(userId) == null || userService.getUserById(targetId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		userFollowService.unfollowUser(userId, targetId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/*
	 * 사용자 운동 기록 조회(월 단위), 운동 기록 추가
	 */
	@Operation(summary = "사용자 운동 기록 조회 (월 단위-캘린더)", description = "사용자의 월별 운동 기록을 조회합니다.")
	@LoginRequired
	@GetMapping("/{userId}/records")
	public List<DailyRecord> getRecordsByMonth(@PathVariable int userId, @RequestParam int year,
	        @RequestParam int month) {
	    return dailyRecordService.getRecordsByMonth(userId, year, month);
	}

	@Operation(summary = "사용자 운동 기록 추가", description = "사용자의 하루 운동 기록을 추가합니다.")
	@LoginRequired
	@PostMapping("/{userId}/records")
	public ResponseEntity<Void> addRecord(@PathVariable int userId, @RequestBody DailyRecord record) {
	    record.setUserId(userId);

	    // date가 비어있으면 에러 처리
	    if (record.getDate() == null) {
	        return ResponseEntity.badRequest().build();
	    }

	    dailyRecordService.addRecord(record);
	    return ResponseEntity.ok().build();
	}
}
