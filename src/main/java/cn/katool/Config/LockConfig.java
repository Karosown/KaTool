/**
 * Title
 *
 * @ClassName: LockConfig
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/4/15 13:22
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.Config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Configuration("LockConfig")
@ToString
@Data
//@ConfigurationProperties("katool.lock")
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Scope("Single")            //  开启单例模式
public class LockConfig {

    @Value("katool.lock")
    private Long internalLockLeaseTime = 30L;


    private TimeUnit timeUnit = TimeUnit.SECONDS;

}
