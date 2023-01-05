/**
 * Title
 *
 * @ClassName: qQiniuConfig
 * @Description: 七牛云配置
 * @author: Karos
 * @date: 2022/10/19 8:32
 * @Blog: https://www.wzl1.top/
 */

package com.karos.KaTool.Config;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration("QiNiuConfig")
@Component
@ToString
@ConfigurationProperties("katool.qiniu")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ComponentScan("com.karos.KaTool.*")
public class QiNiuConfig {
    /**
     * ak
     */
    private String accessKey;
    /**
     * sk
     */
    private String secretKey;
    /**
     * 所属空间
     */
    private String zone;
    /**
     * 基础上传目录
     */
    private String basedir;
    /**
     * 配置空间的存储区域
     * <p>
     * 注意所有Bean首字母小写后，昵称不能一样
     */
    @Bean
    public com.qiniu.storage.Configuration qiNiuConfig() throws Exception {
        switch (zone) {
            case "huadong":
                return new com.qiniu.storage.Configuration(Zone.huadong());
            case "huabei":
                return new com.qiniu.storage.Configuration(Zone.huabei());
            case "huanan":
                return new com.qiniu.storage.Configuration(Zone.huanan());
            case "beimei":
                return new com.qiniu.storage.Configuration(Zone.beimei());
            case "xinjiapo":
                return new com.qiniu.storage.Configuration(Zone.xinjiapo());
            default:
                throw new Exception("存储区域配置错误");
        }
    }

    /**
     * 构建一个七牛上传工具实例
     */
    @Bean
    public UploadManager uploadManager() throws Exception {
        return new UploadManager(qiNiuConfig());
    }

    /**
     * 认证信息实例
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    /**
     * 构建七牛空间管理实例
     */
    @Bean
    public BucketManager bucketManager() throws Exception {
        return new BucketManager(auth(), qiNiuConfig());
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}