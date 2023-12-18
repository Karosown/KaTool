package cn.katool.util.auth;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import cn.katool.constant.AuthConstant;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class AuthUtil {

    static long EXPIRE_TIME;
    static String SALT_KEY;

    public static long getExpireTime() {
        return EXPIRE_TIME;
    }

    public static void setExpireTime(long expireTime) {
        EXPIRE_TIME = expireTime;
    }

    public static String getSaltKey() {
        return SALT_KEY;
    }

    public static void setSaltKey(String saltKey) {
        SALT_KEY = saltKey;
    }

    public static String getToken(HttpServletRequest request){
        if (ObjectUtils.isEmpty(request)){
            throw new RuntimeException("request is null");
        }
        return  request.getHeader(AuthConstant.TOKEN_HEADER);
    }
    /**从
     * 生成Token
     * @param user 用户信息
     * @return token
     */
    public static String createToken(Object user,Class clazz) {
        // 构建header
        log.info("token.header create begin:{}",user);
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        long currentTimeMillis = System.currentTimeMillis();
        Date expireDate = new Date(currentTimeMillis + EXPIRE_TIME);
        header.put("expTime",expireDate.getTime());
        log.info("token.header create end and payload begin:{}",user);
        // 构建payload
        Map<String, Object> payload = new HashMap<>();
        Object payloadUser = null;
        try {
            payloadUser = clazz.newInstance();
            BeanUtils.copyProperties(user,payloadUser);
            payload.put("body", payloadUser);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.addSuppressed(new Exception("[KaTool-Excepton Warning] 请检查user是否为内部类，如果是，可以尝试改为静态内部类或者变为外部类来解决。"));
            throw new RuntimeException(e);
        }
        log.info("token.header payload end and generate Token begin:{},{},{},{}",header,payload,SALT_KEY,user);
        // 生成Token
        String token = JWTUtil.createToken(header, payload, SALT_KEY.getBytes());

        log.info("User:[{}] 生成Token成功！\n" +
                "过期时间为：【{}】\n" +
                "header：\n" +
                "【{}】\n" +
                "payload：\n" +
                "【{}】\n" +
                "Token为：\n" +
                "【{}】", user,expireDate,header, payload,token);

        return token;
    }

    /**
     * 校验Token是否有效
     * @param token Token
     * @return 校验结果
     */
    public static boolean verifyToken(String token) {
        if (StringUtil.isNullOrEmpty(token)||"default".equals(token)){
            return false;
        }
        log.info("token verify:{}",token);
        boolean verify = JWTUtil.verify(token, SALT_KEY.getBytes());
        if (!verify) {
            log.error("Token解析失败，请检查Token是否正确");
            return false;
        }
        if (isExpired(token)){
            log.error("Token已过期，请重新登录");
            return false;
        }
        return true;
    }

    /**
     * 判断Token是否已过期
     * @param token Token
     * @return 过期返回true，未过期返回false
     */
    public static boolean isExpired(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        log.info("jwt:{}",jwt.getHeader());
        Date expTime = new Date(Long.valueOf(jwt.getHeader("expTime").toString()));
        Date currentTime = new Date();
        boolean isBefore = expTime.before(currentTime);
        log.info("expTime:{} , Now:{} , isBefore:{}",expTime,currentTime,isBefore);
        return isBefore;
    }

    /**
     * 解析Token返回User对象
     * @param token Token
     * @return User对象
     */
    public static Object getUserFromToken(String token,Class clazz) {
        if (!verifyToken(token)){
            return null;
        }
        Object body = JSONUtil.toBean((JSONObject) JWTUtil.parseToken(token).getPayload("body"),clazz);
        if (body == null) {
            log.error("Token解析失败，请检查Token是否正确:{}",token);
            return null;
        }
        return body;
    }

}