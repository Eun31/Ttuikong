package com.ttuikong.spring.model.service;

import java.util.Map;

public interface BeanService {
    public void addBeans(int userId, int amount);
    public Map<String, Object> getBeanStatus(int userId) ;
}
