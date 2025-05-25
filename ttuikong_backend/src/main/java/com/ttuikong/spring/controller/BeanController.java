package com.ttuikong.spring.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ttuikong.spring.model.service.BeanService;

@RestController
@RequestMapping("/api/bean")
public class BeanController {

    private final BeanService beanService;
    public BeanController(BeanService beanService) {
		this.beanService = beanService;
	}

    @PutMapping("/{userId}")
    public ResponseEntity<Void> addBeans(@PathVariable int userId, @RequestParam int amount) {
        beanService.addBeans(userId, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getBeanStatus(@PathVariable int userId) {
        return ResponseEntity.ok(beanService.getBeanStatus(userId));
    }
}
