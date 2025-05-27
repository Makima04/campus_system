package com.example.campussysteam.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    // 添加一个方法来检查是否是公开API
    private boolean isPublicApi(String requestUri) {
        // 默认允许的OSS端点，但删除操作需要认证
        if (requestUri.startsWith("/api/oss/") && !requestUri.equals("/api/oss/delete-file")) {
            logger.debug("跳过公开OSS API的JWT认证: {}", requestUri);
            return true;
        }
        
        // 特别记录删除文件请求的认证过程
        if (requestUri.equals("/api/oss/delete-file")) {
            logger.info("检测到OSS删除文件请求，需要JWT认证: {}", requestUri);
            return false;
        }
        
        boolean isPublic = requestUri.startsWith("/api/auth/") || 
               requestUri.startsWith("/api/upload/") || 
               requestUri.startsWith("/file/download/");
               
        if (isPublic) {
            logger.debug("跳过公开API的JWT认证: {}", requestUri);
        }
        
        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        
        // 先检查是否为公开API
        if (isPublicApi(requestUri)) {
            logger.debug("Skipping JWT authentication for public API: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header missing or not Bearer token: URI={}, header={}", 
                       requestUri, authHeader == null ? "null" : authHeader.substring(0, Math.min(10, authHeader.length())) + "...");
            filterChain.doFilter(request, response);
            return;
        }
        
        logger.info("Processing JWT authentication for: {}", requestUri);
        logger.info("Auth header present: {}", authHeader != null);
        
        jwt = authHeader.substring(7);
        try {
            // 先尝试从JWT中提取角色信息进行调试
            Claims claims = jwtService.extractAllClaims(jwt);
            logger.info("JWT claims: {}", claims);
            List<String> rolesFromToken = claims.get("roles", List.class);
            logger.info("Roles from JWT token: {}", rolesFromToken);
            
            userEmail = jwtService.extractUsername(jwt);
            logger.info("Extracted username from JWT: {}", userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = null;
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    logger.info("Loaded UserDetails for: {}", userEmail);
                    logger.info("UserDetails authorities: {}", userDetails.getAuthorities());
                } catch (Exception e) {
                    logger.error("Failed to load UserDetails for username: {}", userEmail, e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid user credentials");
                    return;
                }
                
                // 合并JWT中的角色和用户详情中的角色
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                List<GrantedAuthority> enhancedAuthorities = new ArrayList<>(authorities);
                
                // 从JWT中提取角色信息
                if (rolesFromToken != null && !rolesFromToken.isEmpty()) {
                    // 直接将roles转换为GrantedAuthority列表
                    List<GrantedAuthority> jwtAuthorities = rolesFromToken.stream()
                        .map(role -> new SimpleGrantedAuthority(role.trim()))
                        .collect(Collectors.toList());
                        
                    // 添加JWT中的角色到用户权限中，避免重复
                    for (GrantedAuthority jwtAuth : jwtAuthorities) {
                        if (!enhancedAuthorities.stream().anyMatch(auth -> 
                            auth.getAuthority().equals(jwtAuth.getAuthority()))) {
                            enhancedAuthorities.add(jwtAuth);
                        }
                    }
                    logger.info("Enhanced authorities after adding JWT roles: {}", enhancedAuthorities);
                }
                
                logger.info("Final enhanced authorities: {}", enhancedAuthorities);
                
                boolean isValid = false;
                try {
                    isValid = jwtService.isTokenValid(jwt, userDetails);
                    logger.info("JWT token validation result: {}", isValid);
                } catch (Exception e) {
                    logger.error("JWT token validation error", e);
                }
                
                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            enhancedAuthorities // 使用增强的权限列表
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set in SecurityContext for user: {}", userEmail);
                    logger.info("Authority granted: {}", enhancedAuthorities);
                } else {
                    logger.warn("JWT token validation failed for user: {}", userEmail);
                    // 不设置认证信息，将导致后续的授权检查失败
                }
            } else {
                if (userEmail == null) {
                    logger.warn("Could not extract username from JWT token");
                } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    logger.info("Authentication already set in SecurityContext: {}", 
                              SecurityContextHolder.getContext().getAuthentication());
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT令牌已过期: {}", e.getMessage());
            // 对于非公开API才拒绝访问，公开API应该已在前面放行
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
        } catch (Exception e) {
            logger.error("JWT处理出错: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }
} 