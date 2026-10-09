package com.tynyany.simplewmsv2.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Обработка ошибок бизнес-правил для страниц интерфейса.
 * Ошибки REST API обрабатывает {@link ApiExceptionHandler}.
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(NotFoundException ex) {
        log.warn("Не найдено: {}", ex.getMessage());
        return errorView(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WmsException.class)
    public ModelAndView handleBusiness(WmsException ex) {
        log.warn("Нарушение бизнес-правила: {}", ex.getMessage());
        return errorView(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private ModelAndView errorView(String message, HttpStatus status) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", message);
        mav.addObject("status", status.value());
        mav.setStatus(status);
        return mav;
    }
}
