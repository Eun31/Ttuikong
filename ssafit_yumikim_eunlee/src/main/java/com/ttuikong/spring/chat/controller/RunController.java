package com.ttuikong.spring.chat.controller;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    
    @PostMapping("/upload-map-image")
    public ResponseEntity<?> uploadMapImage(@RequestParam("image") MultipartFile image,
                                            @RequestParam String userId,
                                            @RequestParam String startTime,
                                            @RequestParam String endTime) throws IOException {
        LocalDateTime dateTime = LocalDateTime.parse(startTime); // ISO 형식
        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = userId + "_" + timestamp + ".png";
        
        Path savePath = Paths.get("uploads/maps", filename);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        return ResponseEntity.ok(Map.of("message", "업로드 성공", "filename", filename));
    }

}
