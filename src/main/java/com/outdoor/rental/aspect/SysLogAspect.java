package com.outdoor.rental.aspect;

import com.outdoor.rental.annotation.LogOperation;
import com.outdoor.rental.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class SysLogAspect {

    @Around("@annotation(com.outdoor.rental.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        String operation = logOperation != null ? logOperation.value() : method.getName();

        HttpServletRequest request = currentRequest();
        String ip = resolveClientIp(request);
        String httpMethod = request != null ? request.getMethod() : "-";
        String path = request != null ? request.getRequestURI() : "-";
        String operator = resolveOperator();

        Object result;
        try {
            result = joinPoint.proceed();
            long costMs = System.currentTimeMillis() - start;
            log.info("[操作日志] 操作={} | 操作人={} | IP={} | {} {} | 耗时={}ms | 结果=成功",
                    operation, operator, ip, httpMethod, path, costMs);
            return result;
        } catch (Throwable ex) {
            long costMs = System.currentTimeMillis() - start;
            log.info("[操作日志] 操作={} | 操作人={} | IP={} | {} {} | 耗时={}ms | 结果=失败 | 异常={}",
                    operation, operator, ip, httpMethod, path, costMs, ex.getMessage());
            throw ex;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "-";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser user) {
            return user.getUsername();
        }
        if ("anonymousUser".equals(principal)) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
