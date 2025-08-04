package com.tynyany.simplewmsv2.models;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UpdateMessageAlert {
    public String updateMessage;

    public UpdateMessageAlert(HttpServletRequest request,
                              HttpServletResponse response){
        updateMessage = null;
        String cookieName = "alertMessage";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    // Кука с этим именем есть
                    updateMessage = cookie.getValue().replace('_', ' ');
                    response.addCookie(new DelCookie(cookieName).getCookie());
                }
            }
        }
    }
}
