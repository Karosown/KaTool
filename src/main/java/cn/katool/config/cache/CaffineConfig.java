package cn.katool.config.cache;

import cn.katool.util.cache.utils.CaffeineUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CaffineConfig {

    @Resource
    CacheConfig cacheConfig;
    @Bean(name = "CaffeineUtils")
    @DependsOn({"KaTool-Init"})
    @ConditionalOnMissingBean({CaffeineUtils.class})
    public CaffeineUtils getInstance(@NotNull Cache<String,Object> cache){
        log.info("【Bean工厂】CaffeineUtils => 初始化 CaffeineUtils 实例 {}",cache);
        if (!cache.getClass().getName().equals("com.github.benmanes.caffeine.cache.BoundedLocalCache$BoundedLocalManualCache")){
            log.info("【Bean工厂】CaffeineUtils => cache实例不符，修改当前 CaffeineUtils 实例 {}",cache);
            cache= Caffeine.newBuilder()
                    .expireAfterAccess(cacheConfig.getExpTime(), TimeUnit.MILLISECONDS)
                    .maximumSize(1000)
                    .build();
            log.info("【Bean工厂】CaffeineUtils => cache实例不符，修改为 CaffeineUtils 实例 {}",cache);
        }
        return new CaffeineUtils<>(cache);
    }
}
