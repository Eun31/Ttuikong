package com.ttuikong.spring.model.service;

import java.util.Map;

public interface BeanService {
    public void addBeans(int id, int amount);
    public Map<String, Object> getBeanStatus(int id) ;
}
