package com.ttuikong.spring.exception;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 데이터를 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        log.error("Entity not found: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * 권한이 없는 경우 (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.error("Access denied: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "FORBIDDEN", 
            "권한이 없습니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * 잘못된 요청 데이터 (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.error("Illegal argument: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "BAD_REQUEST", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 데이터 검증 실패 (400)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e, HttpServletRequest request) {
        log.error("Validation error: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * @Valid 어노테이션 검증 실패 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Method argument not valid: {}", e.getMessage());
        
        // 필드별 에러 메시지 수집
        StringBuilder errorMessage = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            if (errorMessage.length() > 0) {
                errorMessage.append(", ");
            }
            errorMessage.append(error.getField()).append(": ").append(error.getDefaultMessage());
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR", 
            errorMessage.toString(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 인증 실패 (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        log.error("Authentication failed: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNAUTHORIZED", 
            "인증에 실패했습니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * JWT 토큰 관련 예외 (401)
     */
    @ExceptionHandler({JwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleJwtException(Exception e, HttpServletRequest request) {
        log.error("JWT error: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_TOKEN", 
            "유효하지 않은 토큰입니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * 중복 데이터 (409)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("Data integrity violation: {}", e.getMessage());
        
        String message = "데이터 중복 또는 제약 조건 위반입니다.";
        if (e.getMessage().contains("email")) {
            message = "이미 사용 중인 이메일입니다.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "CONFLICT", 
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * HTTP 메서드가 지원되지 않는 경우 (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("Method not supported: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "METHOD_NOT_ALLOWED", 
            "지원하지 않는 HTTP 메서드입니다: " + e.getMethod(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    /**
     * 지원하지 않는 미디어 타입 (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.error("Media type not supported: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNSUPPORTED_MEDIA_TYPE", 
            "지원하지 않는 미디어 타입입니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }
    
    /**
     * 요청 파라미터 누락 (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.error("Missing parameter: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER", 
            "필수 파라미터가 누락되었습니다: " + e.getParameterName(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 파일 입출력 오류 (500)
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException e, HttpServletRequest request) {
        log.error("IO Exception occurred: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_IO_ERROR", 
            "파일 처리 중 오류가 발생했습니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * JSON 파싱 오류 (400)
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonProcessing(JsonProcessingException e, HttpServletRequest request) {
        log.error("JSON processing error: {}", e.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "JSON_PARSE_ERROR", 
            "JSON 형식이 올바르지 않습니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 서버 내부 오류 (500) - 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR", 
            "서버 내부 오류가 발생했습니다.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

