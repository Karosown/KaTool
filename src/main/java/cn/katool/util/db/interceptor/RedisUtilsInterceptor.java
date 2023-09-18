package cn.katool.util.db.interceptor;


import cn.katool.util.cache.policy.CachePolicy;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.print.attribute.AttributeSetUtilities;
import java.util.*;

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

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.get*(*))||execution(* cn.katool.util.db.nosql.RedisUtils.get*(*,*))")
    public Object aroundByGet(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        String key = args.get(0).toString();
        // 如果不采取内存缓存策略，那么直接走Redis
        if (!casePolicy()){
            return aroundByGetResponse(joinPoint);
        }
        log.info("RedisUtil-AOP => {}: 命中内存缓存，key:{}", joinPoint.getSignature().getName(),key);
        Object value = cachePolicy.get(key);
        return value;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.getZsetByRange(..))")
    public Object aroundByGetByRange(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        String key = args.get(0).toString();
        Integer start= (Integer) args.get(1);
        Integer end = (Integer) args.get(2);
        // 如果不采取内存缓存策略，那么直接走Redis
        if (!casePolicy()){
            return aroundByGetResponse(joinPoint);
        }
        log.info("RedisUtil-AOP => {}: 命中内存缓存，key:{}", joinPoint.getSignature().getName(),key);
        Map<Object,Double> entries = (Map<Object, Double>) cachePolicy.get(key);
        if (ObjectUtils.isEmpty(entries)){
            log.info("RedisUtil-AOP => {}: 内存缓存为空，命中Redis，key:{}", joinPoint.getSignature().getName(),key);
            return aroundByGetResponse(joinPoint);
        }
        Set<Object> values = entries.keySet();
        List<Object> objects = Arrays.asList(values.toArray()).subList(start, end);
        values.clear();
        objects.forEach(value -> values.add(value));
        return values;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.remove(..))")
    public Object aroundByRemove(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果不采取内存缓存策略，那么直接走Redis
        if (casePolicy()){
            List<Object> args = Arrays.asList(joinPoint.getArgs());
            String key = args.get(0).toString();
            Object value = cachePolicy.get(key);
            if (!ObjectUtils.isEmpty(value)){
                log.info("RedisUtil-AOP => {}: 命中内存缓存，key:{}", joinPoint.getSignature().getName(),key);
                cachePolicy.remove(key);
                log.info("RedisUtil-AOP => {}: 内存缓存删除成功，key:{}", joinPoint.getSignature().getName(),key);
            }
        }
        return aroundByRemoveResponse(joinPoint);
    }

    private Object aroundByRemoveResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        return proceed;
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.set*(..)) ||" +
            " execution(* cn.katool.util.db.nosql.RedisUtils.put*(*,*)) ||" +
            " execution(* cn.katool.util.db.nosql.RedisUtils.push*(*,*))")
    public Object aroundBySetOrPut(ProceedingJoinPoint joinPoint) throws Throwable {

        if (casePolicy()){
            List<Object> args = Arrays.asList(joinPoint.getArgs());
            String key = args.get(0).toString();
            String value = args.get(1).toString();
            cachePolicy.setOrUpdate(key,value);
        }
        return aroundBySETResponse(joinPoint);
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.push*(*,*,*))")
    public Object aroundByPush(ProceedingJoinPoint joinPoint) throws Throwable {
        if (casePolicy()){
            List<Object> args = Arrays.asList(joinPoint.getArgs());
            String key = args.get(0).toString();
            Object colomun = args.get(1).toString();
            Object value = args.get(2).toString();
            Map<Object,Object> map= (Map<Object, Object>) cachePolicy.get(key);
            map.put(colomun,value);
            cachePolicy.setOrUpdate(key,map);
        }
        return aroundBySETResponse(joinPoint);
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.putZset(*,*,Double))")
    public Object aroundByPutZsetByScore(ProceedingJoinPoint joinPoint) throws Throwable {
        if (casePolicy()){
            List<Object> args = Arrays.asList(joinPoint.getArgs());
            String key = args.get(0).toString();
            Object value = args.get(1).toString();
            Double score = Double.valueOf(args.get(2).toString());
            Map<Object,Object> map= (Map<Object, Object>) cachePolicy.get(key);
            if (map==null||map.isEmpty()){
                map.put(value,score);
            }else {
                PriorityQueue<Pair<Double,Object>> queue=new PriorityQueue<>();
                map.entrySet().forEach(entry->{
                    Double v = (Double) entry.getValue();
                    Object k = entry.getKey();
                    queue.add(new Pair<>(v,k));
                });
                queue.add(new Pair<>(score,value));
                map.clear();
                queue.forEach(entry->{
                    Double s = entry.getKey();
                    Object v = entry.getValue();
                    map.put(v,s);
                });
            }
            cachePolicy.setOrUpdate(key,map);
        }
        return aroundBySETResponse(joinPoint);
    }

    @Around("execution(* cn.katool.util.db.nosql.RedisUtils.putZset(*,*))")
    public Object aroundByPutZSet(ProceedingJoinPoint joinPoint) throws Throwable {
        if (casePolicy()){
            List<Object> args = Arrays.asList(joinPoint.getArgs());
            String key = args.get(0).toString();
            Set<ZSetOperations.TypedTuple> entries = (Set<ZSetOperations.TypedTuple>) args.get(1);
            Map<Object,Object> map= (Map<Object, Object>) cachePolicy.get(key);
                PriorityQueue<Pair<Double,Object>> queue=new PriorityQueue<>();
                map.entrySet().forEach(entry->{
                    Double v = (Double) entry.getValue();
                    Object k = entry.getKey();
                    queue.add(new Pair<>(v,k));
                });
                entries.forEach(entry->{
                    Object value = entry.getValue();
                    Double score = entry.getScore();
                    queue.add(new Pair<>(score,value));
                });
                map.clear();
                queue.forEach(entry->{
                    Double s = entry.getKey();
                    Object v = entry.getValue();
                    map.put(v,s);
                });
                cachePolicy.setOrUpdate(key,map);
            }
        return aroundBySETResponse(joinPoint);
    }


    private Object aroundBySETResponse(ProceedingJoinPoint joinPoint) throws Throwable  {
        Object proceed = joinPoint.proceed();
        return proceed;
    }

    public Object aroundByGetResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        if (casePolicy()&&!ObjectUtils.isEmpty(proceed)){
            cachePolicy.setOrUpdate(joinPoint.getArgs()[0].toString(),proceed);
        }
        return proceed;
    }

}
