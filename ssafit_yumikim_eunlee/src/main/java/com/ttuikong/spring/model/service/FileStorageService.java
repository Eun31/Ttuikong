package com.ttuikong.spring.model.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {

	private final ResourceLoader resourceLoader;
	private String uploadPath = "src/main/resources/static/uploads";

	public FileStorageService(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	public void init() {
		try {
			createDirectoryIfNotExists(uploadPath);
		} catch (IOException e) {
			throw new RuntimeException("저장소 위치를 초기화할 수 없습니다.", e);
		}
	}

	private void createDirectoryIfNotExists(String path) throws IOException {
		Path dirPath = Paths.get(path);
		if (!Files.exists(dirPath)) {
			Files.createDirectories(dirPath);
		}
	}

	public String storeFile(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}

		// 원본 파일명에서 확장자 추출
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null) {
			originalFilename = "unknown";
		}

		String extension = "";
		if (originalFilename.lastIndexOf(".") > 0) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		// 고유한 파일명 생성 (UUID + 확장자)
		String filename = UUID.randomUUID().toString() + extension;

		Path targetLocation = Paths.get(uploadPath).resolve(filename);
		// 파일 저장
		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

		// URL 형식으로 저장 경로 반환
		return "/uploads/" + filename;
	}

	public boolean deleteFile(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			return false;
		}

		try {
			// URL에서 파일 경로 추출
			// "/uploads/board/uuid.jpg" -> "board/uuid.jpg"
			String relativePath = fileUrl.replace("/uploads/", "");
			Path filePath = Paths.get(uploadPath).resolve(relativePath);

			// 파일 삭제
			return Files.deleteIfExists(filePath);
		} catch (IOException e) {
			System.err.println("파일 삭제 실패: " + e.getMessage());
			return false;
		}
	}
}
