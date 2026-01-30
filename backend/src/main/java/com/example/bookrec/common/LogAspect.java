package com.example.bookrec.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    // 拦截 controller 包下所有方法
    @Around("execution(* com.example.bookrec.controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("=== 请求开始: [{}] {} ===", method, url);
        logger.info("方法: {}", methodName);
        logger.info("参数: {}", Arrays.toString(args));

        // 执行目标方法
        Object result = joinPoint.proceed();

        long timeTaken = System.currentTimeMillis() - startTime;
        logger.info("=== 请求结束: 耗时 {} ms ===", timeTaken);

        return result;
    }
}