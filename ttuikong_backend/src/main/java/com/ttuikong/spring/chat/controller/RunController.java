package com.ttuikong.spring.chat.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttuikong.spring.annotation.LoginRequired;
import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.chat.model.service.RunService;
import com.ttuikong.spring.model.dto.Route;
import com.ttuikong.spring.model.dto.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    @Autowired
    private RunService runService;

    @Operation(summary = "러닝 후 이미지 자동 업로드")
    @PostMapping("/upload-map-image")
    @LoginRequired
    public ResponseEntity<?> uploadMapImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @Parameter(hidden = true) @LoginUser User loginUser) throws IOException {

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        }

        OffsetDateTime parsedStart = OffsetDateTime.parse(startTime);
        LocalDateTime start = parsedStart.toLocalDateTime().withNano(0);
        
        // 파일 이름 생성
        String timestamp = start.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = loginUser.getId() + "_" + timestamp + ".png";

        // 이미지 저장
        Path savePath = Paths.get("uploads/maps", filename);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        // DB 저장
        String imageUrl = "/uploads/maps/" + filename;
        runService.updateImageUrl(loginUser.getId(), start, imageUrl);

        return ResponseEntity.ok(Map.of(
                "message", "업로드 성공",
                "filename", filename,
                "imageUrl", imageUrl));
    }

    @Operation(summary = "러닝 상태 변화 추적")
    @PostMapping("/running-status")
    @LoginRequired
    public ResponseEntity<?> startRunning(HttpServletRequest request,
                                        @Parameter(hidden = true) @LoginUser User loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String rawJson = new BufferedReader(new InputStreamReader(request.getInputStream()))
                                .lines()
                                .collect(Collectors.joining());

            // System.out.println("받은 JSON: " + rawJson);

            JsonNode node = mapper.readTree(rawJson);
            String startTimeStr = node.get("startTime").asText();
            String status = node.get("status").asText();

            LocalDateTime startTime = OffsetDateTime.parse(startTimeStr).toLocalDateTime();
            runService.insertRunningStatus(loginUser.getId(), startTime, status);

            return ResponseEntity.ok(Map.of("message", "러닝 시작 기록 완료"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 에러: " + e.getMessage()));
        }
    }

    @Operation(summary = "러닝 후 시간과 거리 데이터 저장")
    @PostMapping("/track-location")
    @LoginRequired
    public ResponseEntity<?> trackRunning(HttpServletRequest request,
            @Parameter(hidden = true) @LoginUser User loginUser) {
        
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String rawJson = new BufferedReader(new InputStreamReader(request.getInputStream()))
                                .lines()
                                .collect(Collectors.joining());

            System.out.println("받은 JSON: " + rawJson);

            JsonNode node = mapper.readTree(rawJson);
            String startTimeStr = node.get("startTime").asText();
            String distance = node.get("distance").asText();
            String endTimeStr = node.get("endTime").asText();

            LocalDateTime startTime = OffsetDateTime.parse(startTimeStr).toLocalDateTime().withNano(0);
            LocalDateTime endTime = OffsetDateTime.parse(endTimeStr).toLocalDateTime().withNano(0);
            double distanceDbl = Double.valueOf(distance);

            long duration = Duration.between(startTime, endTime).getSeconds();
            runService.updateRunRecord(loginUser.getId(), startTime, endTime, distanceDbl, duration);

            return ResponseEntity.ok(Map.of("message", "러닝 기록 저장 완료"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 에러: " + e.getMessage()));
        }
    }

    @Operation(summary = "하루 러닝시간 계산")
    @PutMapping("/user/{userId}/day-time")
    @LoginRequired
    public void updateDailyDuration(@PathVariable int userId) {
        runService.updateDailyDuration(userId);
    }

    @Operation(summary = "하루 러닝시간 top10 사용자의 이름과 시간 조회")
    @GetMapping("/rank")
    @LoginRequired
    public List<Map<String, Object>> getTop10UsersByDuration() {
        return runService.getTop10UsersByDuration();
    }

    @Operation(summary = "크루 각 멤버의 이름과 하루 러닝 시간 조회")
    @GetMapping("/crew/{crewId}")
    @LoginRequired
    public List<Map<String, Object>> getCrewMemberRecords(@PathVariable int crewId) {
        return runService.getCrewMemberRecords(crewId);
    }

    @Operation(summary = "크루가 설정한 목표에 따른 평균/총합 러닝 시간 조회")
    @GetMapping("/crew/{crewId}/time")
    @LoginRequired
    public Map<String, Object> getCrewGoalStats(@PathVariable int crewId) {
        return runService.getCrewGoalStats(crewId);
    }

    @Operation(summary = "내 랭킹 정보 조회")
    @GetMapping("/rank/me")
    @LoginRequired
    public Map<String, Object> getMyRanking(@RequestParam int userId) {
        return runService.getMyRanking(userId);
    }
}
