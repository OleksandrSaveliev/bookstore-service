package com.my.bookstore.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Pointcut("execution(* com.my.bookstore.service.impl.*.*(..))")
    public void serviceMethods() {
    }

    @Around(value = "serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String argsStr = truncate(Arrays.toString(joinPoint.getArgs()), 200);

        log.info(">> {}.{}() - Args: {}", className, methodName, argsStr);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            String resultStr = truncate(result != null ? result.toString() : "null", 200);
            log.info("<< {}.{}() - Returned: {} - Exec time: ({}ms)", className, methodName, resultStr, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("✗ {}.{}() threw: {} — {} - Exec time: ({}ms)",
                    className, methodName,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    duration);
            throw ex;
        }
    }

    private String truncate(String str, int len) {
        return str.length() > len ? str.substring(0, len) + "..." : str;
    }
}