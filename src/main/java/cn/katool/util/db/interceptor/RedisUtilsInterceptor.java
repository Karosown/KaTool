package cn.katool.util.db.interceptor;


import cn.katool.util.cache.policy.CachePolicy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
@Aspect
@Component
@Slf4j
public class RedisUtilsInterceptor {

    @Resource
    private CachePolicy cachePolicy;

    public CachePolicy getCachePolicy() {
        return this.cachePolicy;
    }

    public void setCachePolicy(CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
    }
    public Boolean casePolicy() throws Throwable {
        if (ObjectUtils.isEmpty(cachePolicy)||cachePolicy.getClass().getName()=="cn.katool.util.cache.policy.DefaultCachePolicy"){
            return false;
        }
        return true;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.get*(..))")
    public Object aroundByGet(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果不采取内存缓存策略，那么直接走Redis
        if (!casePolicy()){
            return aroundByGetResponse(joinPoint);
        }
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        String key = args.get(0).toString();
        Object value = cachePolicy.get(key);
        if (ObjectUtils.isEmpty(value)){
            return aroundByGetResponse(joinPoint);
        }
        log.info("RedisUtil-AOP => {}: 命中内存缓存，key:{}", joinPoint.getSignature().getName(),key);
        return value;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.remove(..))")
    public Object aroundByRemove(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果不采取内存缓存策略，那么直接走Redis
        if (!casePolicy()){
            return aroundByRemoveResponse(joinPoint);
        }
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        String key = args.get(0).toString();
        Object value = cachePolicy.get(key);
        if (!ObjectUtils.isEmpty(value)){
            log.info("RedisUtil-AOP => {}: 命中内存缓存，key:{}", joinPoint.getSignature().getName(),key);
            cachePolicy.remove(key);
            log.info("RedisUtil-AOP => {}: 内存缓存删除成功，key:{}", joinPoint.getSignature().getName(),key);
        }
        return aroundByRemoveResponse(joinPoint);
    }

    private Object aroundByRemoveResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        return proceed;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.set*(..)) || execution(* cn.katool.util.db.nosql.RedisUtils.put*(..))")
    public Object aroundBySET(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!casePolicy()){
            return aroundByGetResponse(joinPoint);
        }
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        String value = args.get(1).toString();
        return aroundBySETResponse(joinPoint,value);
    }


    private Object aroundBySETResponse(ProceedingJoinPoint joinPoint,Object value) throws Throwable  {
        Object proceed = joinPoint.proceed();
        if (!ObjectUtils.isEmpty(cachePolicy)){
            cachePolicy.setOrUpdate(joinPoint.getArgs()[0].toString(),value);
        }
        return proceed;
    }

    public Object aroundByGetResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        if (!ObjectUtils.isEmpty(cachePolicy)){
            cachePolicy.setOrUpdate(joinPoint.getArgs()[0].toString(),proceed);
        }
        return proceed;
    }

}
