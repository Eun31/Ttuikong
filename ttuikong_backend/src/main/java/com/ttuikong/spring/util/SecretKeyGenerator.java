package com.ttuikong.spring.util;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

// secret key 생성 코드 (1회성)
public class SecretKeyGenerator {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
//        System.out.println("Base64 Encoded Secret Key: " + base64Key);
    }
}
