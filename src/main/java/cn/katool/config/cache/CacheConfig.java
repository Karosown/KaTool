package cn.katool.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("CacheConfig")
@ConfigurationProperties("katool.cache")
public class CacheConfig {

    private Long expTime = 5*60*1000L;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * 策略
     */
    private String policy="default";

    @Bean("katool-cache")
    @DependsOn({"KaTool-Init"})
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

}
