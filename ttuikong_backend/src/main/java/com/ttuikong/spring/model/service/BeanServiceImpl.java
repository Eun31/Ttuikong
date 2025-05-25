package com.ttuikong.spring.model.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ttuikong.spring.chat.model.dao.CrewDao;
import com.ttuikong.spring.chat.model.dao.RunDao;
import com.ttuikong.spring.model.dao.BeanDao;
import com.ttuikong.spring.model.dao.UserDao;
import com.ttuikong.spring.model.dto.Bean;
import com.ttuikong.spring.model.dto.User;

@Service
public class BeanServiceImpl implements BeanService {
    
    @Autowired
    private BeanDao beanDao;

    @Autowired
    private UserDao userDao;

    @Autowired private RunDao runDao;
    @Autowired private CrewDao crewDao;

    @Transactional
    public void addBeans(int id, int amount) {
        Bean bean = new Bean();
        bean.setUserId(id);
        bean.setBeans(amount);
        beanDao.insertBean(bean);

        int totalBeans = beanDao.getTotalBeanByUser(id);

        User user = userDao.selectById(id);
        ActivityLevel currentLevel = ActivityLevel.fromDisplayName(user.getActivityLevel());
        ActivityLevel newLevel = currentLevel;

        while (true) {
            int required = levelUpThresholds.getOrDefault(newLevel, Integer.MAX_VALUE); // 전광석화 치타면 무한 필요
            if (totalBeans < required) break;

            totalBeans -= required;
            newLevel = ActivityLevel.getNextLevel(newLevel);
        }

        if (!newLevel.equals(currentLevel)) {
            userDao.updateActivityLevel(id, newLevel.getDisplayName());
        }

        beanDao.deleteBeansByUserId(id);
        if (totalBeans > 0) {
            Bean remaining = new Bean();
            remaining.setUserId(id);
            remaining.setBeans(totalBeans);
            beanDao.insertBean(remaining);
        }
    }

    public Map<String, Object> getBeanStatus(int userId) {
        int total = beanDao.getTotalBeanByUser(userId);
        User user = userDao.selectById(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("beanCount", total);
        map.put("activityLevel", user.getActivityLevel());
        return map;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void rewardBeansForDailyAchievements() {

        List<User> users = userDao.selectAll();

        for (User user : users) {
            ActivityLevel level = ActivityLevel.fromDisplayName(user.getActivityLevel());
            int multiplier = level.getMultiplier();

            boolean metPersonalGoal = runDao.didMeetRecommendation(user.getId());
            boolean metAnyCrewGoal = crewDao.didMeetAnyCrewGoal(user.getId());

            int beans = 0;
            if (metPersonalGoal) beans += multiplier;
            if (metAnyCrewGoal) beans += multiplier;

            if (beans > 0) {
                addBeans(user.getId(), beans);
            }
        }
    }

        public enum ActivityLevel {
            느긋한_코알라("느긋한 코알라", 5),
            산책하는_거북이("산책하는 거북이", 4),
            신나는_강아지("신나는 강아지", 3),
            힘찬_질주_말("힘찬 질주 말", 2),
            전광석화_치타("전광석화 치타", 1);

            private final String displayName;
            private final int beanMultiplier;

            ActivityLevel(String displayName, int beanMultiplier) {
                this.displayName = displayName;
                this.beanMultiplier = beanMultiplier;
            }

            public String getDisplayName() {
                return displayName;
            }

            public int getMultiplier() {
                return beanMultiplier;
            }

            public static ActivityLevel fromDisplayName(String name) {
                return Arrays.stream(values())
                    .filter(l -> l.displayName.equals(name))
                    .findFirst()
                    .orElse(느긋한_코알라); // 기본값
            }

            public static ActivityLevel getNextLevel(ActivityLevel current) {
                if (current == null) return 느긋한_코알라;
                int next = current.ordinal() + 1;
                return next < values().length ? values()[next] : current; // 마지막 레벨이면 그대로 유지
            }
        }

    private static final Map<ActivityLevel, Integer> levelUpThresholds = new HashMap<>() {{
        put(ActivityLevel.느긋한_코알라, 100);
        put(ActivityLevel.산책하는_거북이, 150);
        put(ActivityLevel.신나는_강아지, 200);
        put(ActivityLevel.힘찬_질주_말, 250);
        // 전광석화_치타는 마지막 레벨이므로 추가 안 해도 됨
    }};

}
