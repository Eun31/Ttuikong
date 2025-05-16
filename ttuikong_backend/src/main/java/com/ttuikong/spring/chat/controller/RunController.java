package com.ttuikong.spring.chat.controller;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ttuikong.spring.annotation.LoginUser;
import com.ttuikong.spring.chat.model.service.RunService;
import com.ttuikong.spring.model.dto.Route;
import com.ttuikong.spring.model.dto.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    @Autowired
    private RunService runService;

    @Operation(summary = "러닝 후 이미지 자동 업로드")
    @PostMapping("/upload-map-image")
    public ResponseEntity<?> uploadMapImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @Parameter(hidden = true) @LoginUser User loginUser) throws IOException {

        // Z(UTC) 포함된 ISO 포맷 처리
        OffsetDateTime parsedStart = OffsetDateTime.parse(startTime);
        // OffsetDateTime parsedEnd = OffsetDateTime.parse(endTime);

        LocalDateTime start = parsedStart.toLocalDateTime();
        // LocalDateTime end = parsedEnd.toLocalDateTime();

        // 파일 이름 생성
        String timestamp = start.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = loginUser.getId() + "_" + timestamp + ".png";

        // 이미지 저장
        Path savePath = Paths.get("uploads/maps", filename);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        // DB 저장
        String imageUrl = "/uploads/maps/" + filename;
        runService.updateImageUrl(loginUser.getId(), startTime, imageUrl);

        return ResponseEntity.ok(Map.of(
                "message", "업로드 성공",
                "filename", filename,
                "imageUrl", imageUrl));
    }

    @Operation(summary = "러닝 상태 변화 추적")
    @PostMapping("/running-status")
    public ResponseEntity<?> startRunning(@RequestBody Map<String, Object> body,
            @Parameter(hidden = true) @LoginUser User loginUser) {
        String startTimeStr = (String) body.get("startTime");
        String status = (String) body.get("status");
        LocalDateTime startTime = OffsetDateTime.parse(startTimeStr).toLocalDateTime();
        runService.insertRunningStatus(loginUser.getId(), startTime, status);
        return ResponseEntity.ok(Map.of("message", "러닝 시작 기록 완료"));
    }

    @Operation(summary = "러닝 후 시간과 거리 데이터 저장")
    @PostMapping("/track-location")
    public ResponseEntity<?> trackRunning(@RequestBody Map<String, Object> body,
            @Parameter(hidden = true) @LoginUser User loginUser) {
        String startTimeStr = (String) body.get("startTime");
        String endTimeStr = (String) body.get("endTime");
        double distance = Double.parseDouble(body.get("distance").toString());

        LocalDateTime start = OffsetDateTime.parse(startTimeStr).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.parse(endTimeStr).toLocalDateTime();
        long duration = Duration.between(start, end).getSeconds();

        runService.updateRunRecord(loginUser.getId(), start, end, distance, duration);

        return ResponseEntity.ok(Map.of("message", "러닝 기록 저장 완료"));
    }

    @Operation(summary = "하루 러닝시간 계산")
    @PutMapping("/user/{userId}/day-time")
    public void updateDailyDuration(@PathVariable int userId) {
        runService.updateDailyDuration(userId);
    }

    @Operation(summary = "하루 러닝시간 top10 사용자의 이름과 시간 조회")
    @GetMapping("/rank")
    public List<Map<String, Object>> getTop10UsersByDuration() {
        return runService.getTop10UsersByDuration();
    }

    @Operation(summary = "크루 각 멤버의 이름과 하루 러닝 시간 조회")
    @GetMapping("/crew/{crewId}")
    public List<Map<String, Object>> getCrewMemberRecords(@PathVariable int crewId) {
        return runService.getCrewMemberRecords(crewId);
    }

    @Operation(summary = "크루가 설정한 목표에 따른 평균/총합 러닝 시간 조회")
    @GetMapping("/crew/{crewId}/time")
    public Map<String, Object> getCrewGoalStats(@PathVariable int crewId) {
        return runService.getCrewGoalStats(crewId);
    }
}
