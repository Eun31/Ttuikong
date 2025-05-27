package com.ttuikong.spring.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ttuikong.spring.model.dto.User;

@Component
public class UserSanitizeUtils {
    
    public static User sanitizeUser(User user) {
        if (user == null) {
            return null;
        }
        
        // 원본 객체 수정 방지를 위해 새로운 객체 생성
        User sanitizedUser = new User();
        sanitizedUser.setId(user.getId());
        sanitizedUser.setEmail(user.getEmail());
        sanitizedUser.setNickname(user.getNickname());
        sanitizedUser.setGender(user.getGender());
        sanitizedUser.setAge(user.getAge());
        sanitizedUser.setHeight(user.getHeight());
        sanitizedUser.setWeight(user.getWeight());
        sanitizedUser.setActivityLevel(user.getActivityLevel());
        sanitizedUser.setActivityGoal(user.getActivityGoal());
        sanitizedUser.setRole(user.getRole());
        sanitizedUser.setAvgDistance(user.getAvgDistance());
        sanitizedUser.setTotalDistance(user.getTotalDistance());
        sanitizedUser.setTotalRuns(user.getTotalRuns());
        
        sanitizedUser.setPassword(null);
        
        return sanitizedUser;
    }

    public static List<User> sanitizeUserList(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        
        return users.stream()
                .map(UserSanitizeUtils::sanitizeUser)
                .collect(Collectors.toList());
    }
}
