package com.bengebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final TokenService tokenService;

    public SecurityConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 创建 JWT 过滤器
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenService);

        http
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF（如使用 JWT，一般会禁用）
                .authorizeHttpRequests(auth -> auth
                        // 使用 AntPathRequestMatcher 来明确匹配的路径
                        .requestMatchers(new AntPathRequestMatcher("/api/user/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/user/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/ws")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/ai/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/room/**")).permitAll()
                        // 其他全部认证
                        .anyRequest().authenticated())
                // 添加 JWT 过滤器，放在基本认证之前
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
