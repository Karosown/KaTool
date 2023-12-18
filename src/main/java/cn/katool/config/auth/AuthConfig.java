package cn.katool.config.auth;

import cn.katool.config.common.KaToolInit;
import cn.katool.util.auth.AuthUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("AuthConfig")
@ConfigurationProperties("katool.auth")
public class AuthConfig extends AuthUtil {
    private long EXPIRE_TIME = 3600 * 24 * 7;
    private String SALT_KEY =  "katool.salt.version::" + KaToolInit.version;

    @Bean
    public void initAuthUtils(){
        AuthUtil.EXPIRE_TIME=getEXPIRE_TIME();
        AuthUtil.SALT_KEY=getSALT_KEY();
    }
}
