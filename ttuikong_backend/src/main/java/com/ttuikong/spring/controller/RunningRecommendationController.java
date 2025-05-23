package com.ttuikong.spring.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.model.dto.RunningRecommendation;
import com.ttuikong.spring.model.dto.User;
import com.ttuikong.spring.model.service.RunningRecommendationService;

@RestController
@RequestMapping("/api/my/running")
public class RunningRecommendationController {
	
	private final RestTemplate restTemplate;
	private final RunningRecommendationService recommendationService;
	
	@Autowired
    public RunningRecommendationController(RestTemplate restTemplate, RunningRecommendationService recommendationService) {
        this.restTemplate = restTemplate; 
        this.recommendationService = recommendationService;
    }
	
	@Value("${flask.api.url}")
	private String flaskApiUrl;
	
	@GetMapping("/recommendation")
	@LoginRequired
	public ResponseEntity<?> getRecommendation(@LoginUser User loginUser){
		try {
            // 최근 유효한 추천이 있는지 먼저 확인
            RunningRecommendation latestRecommendation = 
                recommendationService.getLatestValidRecommendation(loginUser.getId());
            
            // 유효한 추천이 있으면 바로 반환
            if (latestRecommendation != null) {
                return ResponseEntity.ok(latestRecommendation);
            }
            
            // 없으면 새로운 추천 생성
            // 사용자 정보를 Flask에 전송할 요청 객체 생성
            Map<String, Object> request = new HashMap<>();
            request.put("userId", loginUser.getId());
            request.put("nickname", loginUser.getNickname());
            request.put("gender", loginUser.getGender());
            request.put("age", loginUser.getAge());
            request.put("height", loginUser.getHeight());
            request.put("weight", loginUser.getWeight());
            request.put("activityLevel", loginUser.getActivityLevel());
            request.put("activityGoal", loginUser.getActivityGoal());
            request.put("avgDistance", loginUser.getAvgDistance());
            
            // Flask API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                flaskApiUrl + "/api/recommend", entity, Map.class);
            
            // 응답 처리 및 DB에 저장
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                
                // RunningRecommendation 객체 생성
                RunningRecommendation recommendation = new RunningRecommendation();
                recommendation.setUserId(loginUser.getId());
                recommendation.setRecommendedDistance(((Number) data.get("recommendedDistance")).doubleValue());
                recommendation.setEstimatedTime(((Number) data.get("estimatedTime")).intValue());
                recommendation.setEstimatedCalories(((Number) data.get("estimatedCalories")).intValue());
                recommendation.setCreatedAt(LocalDateTime.now());
                recommendation.setExpiresAt(LocalDateTime.now().plusDays(1));
                
                // DB에 저장
                int recommendationId = recommendationService.saveRecommendation(recommendation);
                
                // 저장된 객체 반환
                recommendation.setId(recommendationId);
                return ResponseEntity.ok(recommendation);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("추천 생성 실패");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("추천 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
	}
}