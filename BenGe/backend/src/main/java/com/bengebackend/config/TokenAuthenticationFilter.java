package com.bengebackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Remove the "Bearer " prefix

            try {
                // 解析 Token 并获取用户信息
                // 解析 Token 并获取用户信息
                Integer userId = tokenService.getUserIdFromToken(token);
                String username = tokenService.getUsernameFromToken(token);

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(userId);  // 将 userId 存储在 Authentication 的 details 中

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 如果 token 无效，可以在此处理错误或返回 401 Unauthorized 错误
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);  // 继续过滤请求
    }
}
