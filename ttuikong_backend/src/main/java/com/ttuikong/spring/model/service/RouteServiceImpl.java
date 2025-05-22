package com.ttuikong.spring.model.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ttuikong.spring.model.dao.RouteDao;
import com.ttuikong.spring.model.dto.Route;
import com.ttuikong.spring.model.dto.RoutePoint;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteDao routeDao;

    public RouteServiceImpl(RouteDao routeDao) {
        this.routeDao = routeDao;
    }

    @Override
    @Transactional
    public int saveRoute(Route route) {
        // 기본 유효성 검사
        if (route.getUserId() <= 0) {
            throw new IllegalArgumentException("유효한 사용자 정보가 필요합니다.");
        }

        if (route.getPoints() == null || route.getPoints().isEmpty()) {
            throw new IllegalArgumentException("유효한 경로 데이터가 필요합니다.");
        }

        // 이름이 없는 경우 자동 생성
        if (route.getRouteName() == null || route.getRouteName().trim().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = now.format(formatter);

            route.setRouteName("달리기 - " + formattedDate);
        }

        // 거리가 계산되지 않은 경우 계산
        if (route.getDistance() <= 0) {
            route.setDistance(calculateDistance(route.getPoints()));
        }

        // 시간이 계산되지 않은 경우 계산
        if (route.getDuration() <= 0 && route.getStartTime() != null && route.getEndTime() != null) {
            route.setDuration(calculateDuration(route.getStartTime(), route.getEndTime()));
        }

        // 활동 유형은 무조건 RUNNING으로 설정

        // 데이터베이스에 저장
        routeDao.insertRoute(route);
        return route.getId();
    }

    @Override
    public Route getRoute(int routeId) {
        return routeDao.selectRoute(routeId);
    }

    @Override
    public List<Route> getUserRoutes(int userId) {
        return routeDao.selectUserRoutes(userId);
    }

    @Override
    @Transactional
    public boolean deleteRoute(int routeId) {
        return routeDao.deleteRoute(routeId) > 0;
    }

    @Override
    @Transactional
    public boolean updateRouteName(int routeId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
        }

        return routeDao.updateRouteName(routeId, name) > 0;
    }

    /**
     * 경로 포인트 목록으로부터 총 거리 계산 (Haversine 공식)
     * 
     * @param points 경로 포인트 목록
     * @return 총 거리 (킬로미터)
     */
    private double calculateDistance(List<RoutePoint> points) {
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            RoutePoint p1 = points.get(i);
            RoutePoint p2 = points.get(i + 1);

            totalDistance += haversineDistance(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
        }

        return totalDistance;
    }

    /**
     * Haversine 공식으로 두 지점 간 거리 계산 (단위: km)
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // 지구 반경 (km)
        final double R = 6371;

        // 라디안으로 변환
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rlat1 = Math.toRadians(lat1);
        double rlat2 = Math.toRadians(lat2);

        // Haversine 공식
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rlat1) * Math.cos(rlat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 시작, 종료 시간으로 소요 시간 계산 (초 단위)
     * ISO 8601 형식 시간을 가정 (예: 2024-05-07T09:30:00Z)
     */
    private long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            // // Z가 없는 경우 처리
            // if (!startTime.endsWith("Z"))
            // startTime += "Z";
            // if (!endTime.endsWith("Z"))
            // endTime += "Z";

            // LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            // LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            return java.time.Duration.between(startTime, endTime).getSeconds();
        } catch (Exception e) {
            // 파싱 오류 시 0 반환
            return 0;
        }
    }

    public Integer getLatestRouteIdByUser(int userId) {
        return routeDao.selectLatestRouteId(userId);
    }
}
