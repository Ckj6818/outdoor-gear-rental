package com.outdoor.rental.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 全局路由拦截：保护 {@code /api/**}，放行登录与公开装备查询。
 * <p>
 * 同时开启注解鉴权（如 {@code @SaCheckRole}、{@code @SaIgnore}）。
 * </p>
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/api/**")
                    .notMatch("/api/auth/**")
                    .check(h -> {
                        SaRequest req = SaHolder.getRequest();
                        String path = req.getRequestPath();
                        if (path.startsWith("/api/gears") && "GET".equalsIgnoreCase(req.getMethod())) {
                            return;
                        }
                        StpUtil.checkLogin();
                    });
        })).addPathPatterns("/**");

        // 开启 @SaCheckRole、@SaCheckLogin 等注解鉴权
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
