package com.campus.water.config;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Configuration
public class HttpsConfig {

    // 优先级最高的过滤器，实现HTTP→HTTPS跳转
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter httpToHttpsRedirectFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;

                // 仅处理HTTP请求，跳转到HTTPS
                if ("http".equals(req.getScheme())) {
                    String httpsUrl = "https://" + req.getServerName() + ":" + 8081 + req.getRequestURI();
                    if (req.getQueryString() != null) {
                        httpsUrl += "?" + req.getQueryString();
                    }
                    res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    res.setHeader("Location", httpsUrl);
                    return;
                }
                chain.doFilter(request, response);
            }

            @Override
            public void init(FilterConfig filterConfig) {}

            @Override
            public void destroy() {}
        };
    }
}