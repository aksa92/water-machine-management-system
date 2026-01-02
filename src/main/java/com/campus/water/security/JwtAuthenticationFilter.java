// com/campus/water/security/JwtAuthenticationFilter.java
package com.campus.water.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证拦截器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 提取JWT令牌
            String jwt = jwtTokenProvider.getJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateJwtToken(jwt)) {
                // 获取用户名
                String username = jwtTokenProvider.getUsernameFromJwtToken(jwt);
                // 从JWT中获取角色信息
                String[] roles = jwtTokenProvider.getRolesFromJwtToken(jwt);

                // 创建权限列表
                List<GrantedAuthority> authorities = new ArrayList<>();
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }

                // 加载用户详情（主要用于获取密码等其他信息）
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 设置认证信息，使用从JWT中解析的角色
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("认证失败: ", e);
        }

        filterChain.doFilter(request, response);
    }
}