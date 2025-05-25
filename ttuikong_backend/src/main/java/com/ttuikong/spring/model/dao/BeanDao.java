package com.ttuikong.spring.model.dao;

import com.ttuikong.spring.model.dto.Bean;

public interface BeanDao {
    void insertBean(Bean bean);
    int getTotalBeanByUser(int id);
    void deleteBeansByUserId(int id);
}
