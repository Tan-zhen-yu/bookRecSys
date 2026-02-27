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
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 1. 尝试获取 HTTP 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            // 是正常的 HTTP 请求
            HttpServletRequest request = attributes.getRequest();
            logger.info("=== 请求开始: [{}] {} ===", request.getMethod(), request.getRequestURL());
            logger.info("方法: {}", methodName);
            logger.info("参数: {}", Arrays.toString(args));
        } else {
            // 是非 HTTP 请求（比如我们的单元测试并发调用）

        }

        // 2. ⚡️ 核心：无论什么环境，都必须放行，执行真正的 Controller 方法！
        Object result = joinPoint.proceed();

        // 3. 记录耗时并返回真正的结果
        long timeTaken = System.currentTimeMillis() - startTime;
        logger.info("=== 执行结束: {} 耗时 {} ms ===", methodName, timeTaken);

        return result;
    }
}