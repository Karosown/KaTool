package cn.katool.config.util;

import cn.katool.config.cache.CacheConfig;
import cn.katool.util.lock.LockUtil;
import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.policy.impl.CaffeineCachePolicy;
import cn.katool.util.cache.policy.impl.DefaultCachePolicy;
import cn.katool.util.cache.utils.CaffeineUtils;
import cn.katool.util.db.nosql.RedisUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration("RedisUtilConfig")
@ConfigurationProperties("katool.redis")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Scope("singleton")            //  开启单例模式
public class RedisUtilConfig {

    @Resource
    CacheConfig cacheConfig;

    private String policy="default";    // 不写采用默认策略，默认情况下缓存Cache使用的策略

    // 采用的缓存策略
    @Bean("katool-redisutil-cachepolicy")
    @DependsOn({"KaTool-Init"})
    @ConditionalOnMissingBean({CachePolicy.class})
    public CachePolicy cachePolicy() {
        if ("default".equals(policy)){
            policy = cacheConfig.getPolicy();
        }
        switch (policy) {
            case "caffeine":
                log.info("【Bean工厂】katool-redisutil-cachepolicy=>使用Caffeine缓存策略");
                // 这里面的CaffeineUtil会进行自动装配
                return new CaffeineCachePolicy();
            default:
                log.info("【Bean工厂】katool-redisutil-cachepolicy=>使用默认策略，直接走redis");
                return new DefaultCachePolicy();
        }
    }



    @Resource
    RedisTemplate redisTemplate;
    @Bean
    @DependsOn({"KaTool-Init"})
    public RedisUtils RedisUtils(){
        return RedisUtils.getInstance(redisTemplate);
    }

    @Bean
    @DependsOn({"KaTool-Init"})
    public LockUtil LockUtil(){
        return LockUtil.getInstance();
    }


}