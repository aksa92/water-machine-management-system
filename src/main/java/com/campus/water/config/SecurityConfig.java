// com/campus/water/config/SecurityConfig.java
package com.campus.water.config;

import com.campus.water.security.JwtAuthenticationFilter;
import com.campus.water.security.RoleConstants;
import com.campus.water.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级权限控制
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF
                .csrf(csrf -> csrf.disable())
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限控制
                .authorizeHttpRequests(auth -> auth
                        // 登录接口放行
                        .requestMatchers("/api/app/student/login", "/api/app/repair/login", "/api/web/login").permitAll()
                        // 静态资源放行
                        .requestMatchers("/static/**", "/templates/**").permitAll()
                        // 新增告警接口权限控制（URL级）
                        .requestMatchers("/api/alerts/**").hasAnyRole("ADMIN", "REPAIRMAN")
                        // 基础权限控制（细粒度在Controller层通过注解控制）
                        .requestMatchers("/api/app/student/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/app/repair/**").hasAnyRole("REPAIRMAN", "ADMIN")
                        .requestMatchers("/api/web/**").hasRole("ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 设置认证提供者
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}