// com/campus/water/config/SecurityConfig.java
package com.campus.water.config;

import com.campus.water.security.JwtAuthenticationFilter;
import com.campus.water.security.UserDetailsServiceImpl;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级权限控制
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造函数注入（替代@Autowired字段注入，更安全）
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS并配置源
                .csrf(csrf -> csrf.disable()) // 关闭CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态会话
                .authorizeHttpRequests(auth -> auth
                        // 登录接口放行
                        .requestMatchers("/api/app/student/login", "/api/app/repair/login", "/api/web/login", "/api/common/login").permitAll()
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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 添加JWT过滤器

        http.authenticationProvider(authenticationProvider()); // 设置认证提供者

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许前端源（开发环境）
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许所有HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带凭证（如Cookie）
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（1小时）
        configuration.setMaxAge(3600L);
        // 暴露响应头（便于前端获取自定义头信息）
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 应用到所有API路径
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}