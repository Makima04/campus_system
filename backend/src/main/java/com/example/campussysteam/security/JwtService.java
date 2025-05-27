package com.example.campussysteam.security;

import com.example.campussysteam.module.redis.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT服务类
 * 处理JWT令牌的生成、验证和解析
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final RedisService redisService;
    private static final String TOKEN_PREFIX = "token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public JwtService(RedisService redisService) {
        this.redisService = redisService;
        logger.info("JwtService初始化，redisService: {}", redisService);
    }

    /**
     * 从令牌中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从令牌中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取令牌中的所有声明
     * 修改为public以便在过滤器中调用
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 检查令牌是否已过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为用户生成令牌
     */
    public String generateToken(UserDetails userDetails) {
        String token = generateToken(new HashMap<>(), userDetails);
        // 将token存储到Redis中，设置过期时间
        String key = TOKEN_PREFIX + userDetails.getUsername();
        logger.info("生成令牌并存储到Redis: username={}, key={}", userDetails.getUsername(), key);
        try {
            redisService.set(key, token, jwtExpiration / 1000);
            logger.info("令牌成功存储到Redis: username={}, key={}, expiration={}秒", 
                      userDetails.getUsername(), key, jwtExpiration / 1000);
        } catch (Exception e) {
            logger.error("存储令牌到Redis失败: username={}, key={}", userDetails.getUsername(), key, e);
            throw e;
        }
        return token;
    }

    /**
     * 使用额外声明为用户生成令牌
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        // 添加用户角色信息
        Map<String, Object> claims = new HashMap<>(extraClaims);
        
        // 获取用户的权限列表并添加到claims中
        if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
            // 提取角色信息并添加到claims中
            List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
            
            claims.put("roles", roles);
        }
        
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        logger.debug("验证令牌: token={}, username={}, userDetails.username={}", 
                   token.substring(0, Math.min(10, token.length())) + "...", 
                   username, userDetails.getUsername());
        
        // 检查token是否在黑名单中
        String blacklistKey = BLACKLIST_PREFIX + token;
        boolean inBlacklist = false;
        try {
            inBlacklist = redisService.exists(blacklistKey);
            logger.debug("检查令牌是否在黑名单中: key={}, inBlacklist={}", blacklistKey, inBlacklist);
        } catch (Exception e) {
            logger.error("检查令牌黑名单失败: key={}", blacklistKey, e);
            // 如果Redis出现异常，保守处理，认为令牌不在黑名单中
        }
        
        if (inBlacklist) {
            logger.warn("令牌在黑名单中，拒绝访问: username={}", username);
            return false;
        }
        
        // 检查Redis中的token是否匹配
        String redisKey = TOKEN_PREFIX + username;
        String storedToken = null;
        try {
            storedToken = (String) redisService.get(redisKey);
            logger.debug("从Redis获取存储的令牌: key={}, tokenExists={}", 
                       redisKey, storedToken != null);
            if (storedToken != null) {
                logger.debug("存储的令牌前10个字符: {}", 
                           storedToken.substring(0, Math.min(10, storedToken.length())) + "...");
                logger.debug("当前令牌前10个字符: {}", 
                           token.substring(0, Math.min(10, token.length())) + "...");
                logger.debug("令牌匹配结果: {}", storedToken.equals(token));
            }
        } catch (Exception e) {
            logger.error("从Redis获取令牌失败: key={}", redisKey, e);
            // 如果Redis出现异常，继续其他验证
        }
        
        // 检查用户名匹配
        boolean usernameMatches = username.equals(userDetails.getUsername());
        logger.debug("用户名匹配结果: {}", usernameMatches);
        
        // 检查令牌是否过期
        boolean tokenExpired = isTokenExpired(token);
        logger.debug("令牌是否过期: {}", tokenExpired);
        
        // 完整验证
        boolean isValid = false;
        
        // 如果Redis中有存储的令牌，则需要完全匹配
        if (storedToken != null) {
            isValid = storedToken.equals(token) && usernameMatches && !tokenExpired;
            logger.info("令牌验证结果(使用Redis): username={}, isValid={}", username, isValid);
        } else {
            // Redis中没有存储的令牌，只验证用户名和过期时间
            // 这是一个降级策略，当Redis不可用时仍然可以工作
            isValid = usernameMatches && !tokenExpired;
            logger.warn("Redis中无存储令牌，使用降级验证策略: username={}, isValid={}", username, isValid);
        }
        
        return isValid;
    }

    /**
     * 将token加入黑名单
     */
    public void invalidateToken(String token) {
        String username = extractUsername(token);
        logger.info("将令牌加入黑名单: username={}", username);
        
        try {
            // 将token加入黑名单
            redisService.set(BLACKLIST_PREFIX + token, "invalid", jwtExpiration / 1000);
            logger.info("令牌已加入黑名单: username={}", username);
            
            // 删除用户的token
            redisService.delete(TOKEN_PREFIX + username);
            logger.info("已删除用户的令牌记录: username={}", username);
        } catch (Exception e) {
            logger.error("将令牌加入黑名单失败: username={}", username, e);
            throw e;
        }
    }
} 