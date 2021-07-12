package com.example.smartalbum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC配置器
 *
 * @author Administrator
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * 登录拦截器
     */
    static class LoginInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (request.getSession().getAttribute("userInfo") != null) {
                return true;
            }
            request.getRequestDispatcher("/index").forward(request, response);
            return false;
        }
    }

    /**
     * 添加拦截器,防止直接访问某页面或接口
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**")
                //以下不拦截
                .excludePathPatterns("/", "/index.html", "/index", "/login", "/register", "/**/*.css",
                        "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif",
                        "/**/fonts/*", "/**/*.svg", "/druid/*", "*/druid", "/doesUserExist",
                        "/register", "/sendcode");
    }
}
