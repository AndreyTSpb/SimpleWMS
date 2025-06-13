package com.tynyany.simplewmsv2.models;

import jakarta.servlet.http.Cookie;
import lombok.Getter;

@Getter
public class DelCookie {
    private Cookie cookie;
    public DelCookie(String key){
        Cookie cookie = new Cookie(key, "");
        cookie.setPath("/"); // обязательно указывайте путь, если он был указан при создании
        cookie.setMaxAge(0);
        this.cookie = cookie;
    }
}
