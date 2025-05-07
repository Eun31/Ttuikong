package com.ttuikong.spring.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ttuikong.spring.model.dto.Route;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.RouteService;

@RestController
@RequestMapping("/api/my/route")
public class RouteController {
	private final RouteService routeService;
	
	public RouteController(RouteService routeService) {
		this.routeService = routeService;
	}
	
	// 운동 루트 저장
	@PostMapping
	@LoginRequired
	public ResponseEntity<?> saveRoute(@RequestBody Route route, @LoginUser User loginUser){
		try {
			route.setUserId(loginUser.getId());
			
			int routeId = routeService.saveRoute(route);
			
			Map<String, Object> response = new HashMap<>();
			response.put("message", "운동 루트가 성공적으로 저장되었습니다.");
			response.put("routeId", routeId);
			
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch(IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("루트 저장 중 오류가 발생했습니다.");
		}
	}
	
	// 운동 루트 목록 조회 (로그인한 사용자의 모든 운동 기록 조회)
	@GetMapping
	@LoginRequired
	public ResponseEntity<?> getUserRoutes(@LoginUser User loginUser) {
		try {
			List<Route> routes = routeService.getUserRoutes(loginUser.getId());
			
			if(routes.isEmpty()) {
				Map<String, String> response = new HashMap<>();
				response.put("message", "저장된 운동 루트가 없습니다.");
				return ResponseEntity.ok(response);
			}
			
			return ResponseEntity.ok(routes);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("루트 목록 조회 중 오류가 발생했습니다.");
		}
	}
	
	
	//특정 루트 조회
	@GetMapping("/{routeId}")
	@LoginRequired
	public ResponseEntity<?> getRoute(@PathVariable("routeId") int routeId, @LoginUser User loginUser){
		try {
			Route route = routeService.getRoute(routeId);
			
			if(route == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("해당 루트를 찾을 수 없습니다.");
			}
			if(route.getUserId() != loginUser.getId() && !"ADMIN".equals(loginUser.getRole())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("이 루트에 접근할 권한이 없습니다.");
			}
			
			return ResponseEntity.ok(route);
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("루트 조회 중 오류가 발생했습니다.");
		}
	}
	
	// 운동 루트 삭제
	@DeleteMapping("/{routeId}")
	@LoginRequired
	public ResponseEntity<?> deleteRoute(@PathVariable("routeId") int routeId, @LoginUser User loginUser){
		try {
			Route route = routeService.getRoute(routeId);
			
			if(route == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("해당 루트를 찾을 수 없습니다.");
			}
			
			if(route.getUserId() != loginUser.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("이 루트를 삭제할 권한이 없습니다.");
			}
			
			boolean isDeleted = routeService.deleteRoute(routeId);
			
			if(isDeleted) {
				Map<String, String> response = new HashMap<>();
				response.put("message", "루트가 성공적으로 삭제되었습니다.");
				return ResponseEntity.ok(response);
			}
			else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("루트 삭제에 실패했습니다.");
			}
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("루트 삭제 중 오류가 발생했습니다.");
		}
	}
	
	// 루트 이름 수정
	@PutMapping("/{routeId}/name")
	@LoginRequired
	public ResponseEntity<?> updateRouteName(@PathVariable("routeId") int routeId, @RequestBody Map<String, String> payload, @LoginUser User loginUser){
		try {
			Route route = routeService.getRoute(routeId);
			
			if(route == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("해당 루트를 찾을 수 없습니다.");
			}
			
			if(route.getUserId() != loginUser.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("이 루트를 수정할 권한이 없습니다");
			}
			
			String newName = payload.get("name");
			if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("유효한 이름이 필요합니다.");
            }
            
            boolean isUpdated = routeService.updateRouteName(routeId, newName);
            
            if (isUpdated) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "루트 이름이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("루트 이름 수정에 실패했습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("루트 이름 수정 중 오류가 발생했습니다.");
        }
	}
}
