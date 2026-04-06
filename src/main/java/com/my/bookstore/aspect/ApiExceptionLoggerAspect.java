package com.my.bookstore.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiExceptionLoggerAspect {

    @AfterReturning(
            pointcut = "execution(* com.my.bookstore.exception.GlobalExceptionHandler.*(..))",
            returning = "response"
    )
    public void logGlobalExceptions(JoinPoint joinPoint, Object response) {
        if (response instanceof ResponseEntity<?> responseEntity) {
            int status = responseEntity.getStatusCode().value();
            Object body = responseEntity.getBody();
            String handlerMethod = joinPoint.getSignature().getName();

            if (status >= 500) {
                log.error("[API ERROR] 500 Internal Server Error in {} | Body: {}", handlerMethod, body);
            } else if (status >= 400) {
                log.warn("[API WARN] {} {} in {} | Body: {}", status,
                        responseEntity.getStatusCode().value(), handlerMethod, body);
            }
        }
    }
}