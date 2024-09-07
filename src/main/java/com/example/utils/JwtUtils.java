package com.example.utils;



import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;



    /**
     * 让指定Jwt令牌失效
     * @param headerToken 请求头中携带的令牌
     * @return 是否操作成功
     */
    public boolean invalidateJwt(String headerToken){
        // 获取请求头真正token
        String token = this.convertToken(headerToken);
        // 使用算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = com.auth0.jwt.JWT.require(algorithm).build();
        try {
            // 解密token
            DecodedJWT verify = jwtVerifier.verify(token);
            // 删除
            return deleteToken(verify.getId(), verify.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 根据配置快速计算过期时间
     * @return 过期时间
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire);
        return calendar.getTime();
    }

    /**
     * 根据UserDetails生成对应的Jwt令牌
     * @param user 用户信息
     * @return 令牌
     */
    public String createJwt(UserDetails user, String username, int userId) {

            Algorithm algorithm = Algorithm.HMAC256(key);
            Date expire = this.expireTime();
            return com.auth0.jwt.JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", userId)
                .withClaim("name", username)
                .withClaim("authorities", user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);

    }

    /**
     * 解析Jwt令牌
     * @param headerToken 请求头中携带的令牌
     * @return DecodedJWT
     */
    public DecodedJWT resolveJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        // 检验jwt是否有效
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            // 解码 token
            DecodedJWT verify = jwtVerifier.verify(token);
            if(this.isInvalidToken(verify.getId())) return null;
            // 获取属性
            Map<String, Claim> claims = verify.getClaims();
            // 判断是否过期
            return new Date().after(claims.get("exp").asDate()) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 将jwt对象中的内容封装为UserDetails
     * @param jwt 已解析的Jwt对象
     * @return UserDetails
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
            .withUsername(claims.get("name").asString())
            .password("******")
            .authorities(claims.get("authorities").asArray(String.class))
            .build();
    }

    /**
     * 将jwt对象中的用户ID提取出来
     * @param jwt 已解析的Jwt对象
     * @return 用户ID
     */
    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }



    /**
     * 校验并转换请求头中的Token令牌
     * @param headerToken 请求头中的Token
     * @return 转换后的令牌
     */
    private String convertToken(String headerToken){
        // 去掉Bearer
        if(headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    /**
     * 将Token列入Redis黑名单中
     * @param uuid 令牌ID
     * @param time 过期时间
     * @return 是否操作成功
     */
    private boolean deleteToken(String uuid, Date time){
        if(this.isInvalidToken(uuid))
            return false;
        // 获取当前时间
        Date now = new Date();
        // 判断是否过期
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        // 存储到redis中
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 验证Token是否被列入Redis黑名单
     * @param uuid 令牌ID
     * @return 是否操作成功
     */
    private boolean isInvalidToken(String uuid){
        // 判断redis是否存在token
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
}
