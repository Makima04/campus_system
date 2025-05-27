package com.example.campussysteam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 方法级安全配置
 * 使用自定义RoleVoter来支持不同格式的角色
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    public RoleVoter roleVoter() {
        // 创建一个自定义的角色投票器，支持两种角色格式
        return new RoleVoter() {
            @Override
            public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
                if (authentication == null) {
                    return ACCESS_DENIED;
                }
                
                // 获取用户拥有的所有角色
                Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();
                
                // 遍历所有需要的角色
                for (ConfigAttribute attribute : attributes) {
                    if (attribute.getAttribute() == null) {
                        continue;
                    }
                    
                    String role = attribute.getAttribute();
                    if (!role.startsWith(getRolePrefix())) {
                        continue;
                    }
                    
                    // 从属性中提取角色名称
                    String neededRole = role.substring(getRolePrefix().length());
                    
                    // 检查用户是否拥有这个角色（两种格式都检查）
                    boolean hasRole = userAuthorities.stream().anyMatch(authority -> {
                        String userRole = authority.getAuthority();
                        
                        // 检查完整形式：如ROLE_ADMIN
                        if (userRole.equals(role)) {
                            return true; 
                        }
                        
                        // 检查简写形式：如ADMIN
                        if (userRole.equals(neededRole)) {
                            return true;
                        }
                        
                        // 检查反向情况：用户有ROLE_ADMIN时，需要ADMIN角色
                        if (userRole.startsWith(getRolePrefix()) && 
                            userRole.substring(getRolePrefix().length()).equals(neededRole)) {
                            return true;
                        }
                        
                        return false;
                    });
                    
                    if (hasRole) {
                        return ACCESS_GRANTED;
                    }
                }
                
                return ACCESS_DENIED;
            }
        };
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        
        // 添加我们自定义的角色投票器
        decisionVoters.add(roleVoter());
        
        // 添加默认的网页表达式投票器
        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(new DefaultWebSecurityExpressionHandler());
        decisionVoters.add(webExpressionVoter);
        
        return new AffirmativeBased(decisionVoters);
    }
} 