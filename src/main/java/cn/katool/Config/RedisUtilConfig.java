package cn.katool.Config;

import cn.katool.lock.LockUtil;
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
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration("RedisUtilConfig")
@ConfigurationProperties("katool.util.redis")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Scope("singleton")            //  开启单例模式
public class RedisUtilConfig {


    Long expTime = 5*60*1000L;
    TimeUnit  timeUnit = TimeUnit.MILLISECONDS;

    /**
     * 策略
     */
    String policy="default";
    @Bean("katool-redisutil-cache")
    @ConditionalOnMissingBean({Cache.class})
    public Cache<String, Object> Cache() {
        Cache<String, Object> build=null;
        switch (policy){
            default:    // 默认使用Caffeine
                log.info("【Bean工厂】katool-redisutil-cache=>使用默认Bean策略 使用Caffeine缓存，continue;");
            case "caffeine":
               build = Caffeine.newBuilder()
                        .expireAfterAccess(expTime, timeUnit)
                        .maximumSize(1000)
                        .build();
                log.info("【Bean工厂】katool-redisutil-cache=>使用Caffeine缓存策略 使用Caffeine缓存 建立Cache：{}",build);
                break;
        }
        return build;
    }

    @Bean("katool-redisutil-cachepolicy")
    @ConditionalOnMissingBean({CachePolicy.class})
    public CachePolicy cachePolicy() {
        // 选择caffeine作为内存策略
        switch (policy) {
            case "caffeine":
                log.info("【Bean工厂】katool-redisutil-cachepolicy=>使用Caffeine缓存策略");
                return new CaffeineCachePolicy();
            default:
                log.info("【Bean工厂】katool-redisutil-cachepolicy=>使用默认策略，直接走redis");
                return new DefaultCachePolicy();
        }
    }

    @Bean(name = "CaffeineUtils")
    @ConditionalOnMissingBean({CaffeineUtils.class})
    public CaffeineUtils<String,Object> getInstance(@NotNull Cache<String,Object> cache){
        log.info("【Bean工厂】CaffeineUtils => 初始化 CaffeineUtils 实例 {}",cache);
        if (!cache.getClass().getName().equals("com.github.benmanes.caffeine.cache.BoundedLocalCache$BoundedLocalManualCache")){
            log.info("【Bean工厂】CaffeineUtils => cache实例不符，修改当前 CaffeineUtils 实例 {}",cache);
            cache=Caffeine.newBuilder()
                    .expireAfterAccess(expTime, TimeUnit.MILLISECONDS)
                    .maximumSize(1000)
                    .build();
            log.info("【Bean工厂】CaffeineUtils => cache实例不符，修改为 CaffeineUtils 实例 {}",cache);
        }
        return new CaffeineUtils<>(cache);
    }

    @Resource
    RedisTemplate redisTemplate;
    @Bean
    public RedisUtils RedisUtils(){
        return RedisUtils.getInstance(redisTemplate);
    }

    @Bean
    public LockUtil LockUtil(){
        return LockUtil.getInstance();
    }
}