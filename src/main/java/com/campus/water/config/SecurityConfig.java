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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity() // 启用方法级权限控制
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造函数注入
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
    // com/campus/water/config/SecurityConfig.java
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 替换为MD5加密器
        return new MD5PasswordEncoder();
    }
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤链 - 优先处理CORS和预检请求
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 优先配置CORS，确保预检请求不被拦截
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. 关闭CSRF（配合无状态会话）
                .csrf(csrf -> csrf.disable())
                // 3. 配置会话管理（无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. 配置URL权限
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口
                        .requestMatchers("/api/app/student/login", "/api/app/repair/login", "/api/web/login", "/api/common/login").permitAll()
                        // 放行注册接口
                        .requestMatchers("/api/common/register").permitAll()
                        // 放行静态资源
                        .requestMatchers("/static/**", "/templates/**").permitAll()
                        // 放行预检请求（重要：避免OPTIONS请求被拦截）
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        // 告警接口权限
                        .requestMatchers("/api/alerts/**").hasAnyRole("ADMIN", "REPAIRMAN")
                        // 学生接口权限
                        .requestMatchers("/api/app/student/**").hasAnyRole("STUDENT", "ADMIN")
                        // 维修人员接口权限
                        .requestMatchers("/api/app/repair/**").hasAnyRole("REPAIRMAN", "ADMIN")
                        // 管理员接口权限
                        .requestMatchers("/api/web/**").hasRole("ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // 5. 添加JWT过滤器（在用户名密码过滤器之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 6. 设置认证提供者
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * CORS配置源 - 放宽跨域限制以兼容前端请求
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的前端源（生产环境建议指定具体域名）
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头（包含自定义头）
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        // 允许携带凭证（如Cookie、JWT）
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（减少预检请求次数）
        configuration.setMaxAge(3600L);
        // 暴露响应头（便于前端获取自定义头）
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Length"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 应用到所有路径
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}