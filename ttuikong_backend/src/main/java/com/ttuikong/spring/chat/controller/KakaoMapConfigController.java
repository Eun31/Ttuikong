package com.ttuikong.spring.chat.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class KakaoMapConfigController {

    @Value("${kakao.map.js-key}")
    private String kakaoMapKey;

    @GetMapping("/kakao-map-key")
    public String getKakaoMapKey() {
        return kakaoMapKey;
    }
}
