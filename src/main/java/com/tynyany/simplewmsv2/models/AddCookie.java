package com.tynyany.simplewmsv2.models;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class AddCookie {

    private Cookie cookie;
    public AddCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/"); // устанавливаем путь, если нужно
        cookie.setMaxAge(7 * 24 * 60 * 60); // срок действия в секундах, например, 7 дней
        this.cookie = cookie;
    }
}
