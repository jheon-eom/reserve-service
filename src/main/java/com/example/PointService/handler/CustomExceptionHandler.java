package com.example.PointService.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDto> apiValidationException(RuntimeException e) {
        log.error(e.getMessage());

        return ResponseEntity.badRequest().body(new ExceptionDto(e.getClass().getName(), e.getMessage()));
    }
}
