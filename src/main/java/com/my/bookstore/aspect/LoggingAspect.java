package com.my.bookstore.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.my.bookstore.service.impl.*.*(..))")
    public void serviceMethods() {
    }

    @Around(value = "serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String argsStr = Arrays.toString(joinPoint.getArgs());

        argsStr = truncate(argsStr, 200);

        log.info(">> {}.{}() - Args: {}", className, methodName, argsStr);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        String resultStr = result != null ? result.toString() : "null";

        resultStr = truncate(resultStr, 200);

        log.info("<< {}.{}() - Returned: {} - Exec time: ({}ms)", className, methodName, resultStr, duration);

        return result;
    }

    private String truncate(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len) + "...";
        }
        return str;
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        log.error("✗ {}.{}() threw: {} — {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}