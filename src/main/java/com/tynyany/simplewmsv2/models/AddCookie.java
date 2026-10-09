package com.tynyany.simplewmsv2.models;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class AddCookie {

    private Cookie cookie;
    public AddCookie(String key, String value) {

        // Пробелы заменяются на '_' (обратная замена в UpdateMessageAlert),
        // символы, недопустимые в значении cookie (RFC 6265), удаляются
        String safeValue = value == null ? "" : value.replace(' ', '_').replaceAll("[\",;\\\\\\p{Cntrl}]", "");
        Cookie cookie = new Cookie(key, safeValue);
        cookie.setPath("/"); // устанавливаем путь, если нужно
        cookie.setMaxAge(7 * 24 * 60 * 60); // срок действия в секундах, например, 7 дней
        this.cookie = cookie;
    }
}
