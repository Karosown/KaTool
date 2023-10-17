<img src="http://7n.cdn.wzl1.top/typora/img/KaTool.png" alt="KaTool" style="zoom:200%;" />

&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;![SpringBoot-2.7.0](https://img.shields.io/badge/SpringBoot-2.7.0-green)&emsp;&emsp;&emsp;![Hutool-5.8.10](https://img.shields.io/badge/Hutool-5.8.10-green)

# KaTool (卡托)
KaTool - 一款拥有七牛云文件处理、IP工具、IO工具、图形验证码生成、随机验证码生成、Date工具、Base64工具、函数式接口、分布式锁实现的Java开发工具类，依赖于SpringBoot框架<br>

**Tips：该Starter为个人项目使用，Starter制作仅满足于个人目前的开发需求，也只是个人开发，目前还在孵化阶段，工具类和其他同类作品相比不全面望谅解，在图片写入OutputStream时，使用到了Hutool**
## 安装Katool
### git clone(仅第一次使用)
```shell
git clone https://github.com/Karosown/KaTool.git
```
### 打开项目
选择Maven install
![image-20230105233852328](http://7n.cdn.wzl1.top/typora/img/image-20230105233852328.png)
### pom.xml
在自己的项目中引入依赖
```xml
<!-- https://mvnrepository.com/artifact/cn.katool/KaTool -->
<dependency>
    <groupId>cn.katool</groupId>
    <artifactId>KaTool</artifactId>
    <*version>{{KaTool.version}}</version>
</dependency>
```
## Application.yml配置说明

使用分布式锁前请开启Redistemplate的事务支持

```yaml
katool:
  # 七牛云配置 所有值都必须存在,没有的话留空,不能缺
      qiniu:
        accessKey: #你的七牛云accessKey
        secretKey: #你的七牛云secretKey
        # 对象储存
        bucket: # 空间名称
        zone: # 存储区域
        domain: # 访问域名
        basedir: # 文件存储根目录
    lock:
        internalLockLeaseTime: 30   # 分布式锁默认租约时间，建议别设太小，不然和没有设置毫无区别
        timeUnit: seconds           # 租约时间单位
    util:
        redis:
            policy: "caffeine"      # 选择内存缓存策略，caffeine
            exptime: {5*60*1000}              # LFU过期时间
            time-unit: milliseconds #  过期时间单位
```
## Nginx配置
Nginx反向代理后获取真实来源IP
```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
## RedisUtilConfig

如何自定义Redis多层缓存策略

```java
package cn.katool.katooltest.config;


import cn.katool.util.cache.policy.CachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;



@Configuration
public class RedisUtilConfig {

    

    @Bean
    @DependsOn({"KaTool-Init"})
    @Primary
    public CachePolicy cachePolicy() {

        return null;
    }


}
```

如果使用默认策略（不采取缓存）直接`return new DefaultCachePolicy();`即可，KaTool自动装配了Caffeine的Cache，所以可以直接对CaffeineUtil进行使用

## Update
v1.9.5 - ALPHA 2023 / 10 / 16
- 将RedisUtil并行获取ZSet数据加入日程
v1.9.4 - Release 2023 / 09 / 24
- 添加@ConditionalOnMissingBean注解，用户自定义配置不需要使用@Primary注解
- 解决内存取得null，不名字Redis的问题
v1.9.3 2023 / 09 /18

- 修改RedisUtil中Zset为ZSet，更符合命名标准

v1.9.2 2023 / 09 / 18

- 解决RedisUtil中，多参数多级分类策略异常问题

v1.9.1 2023 / 09 / 13

- 新增掠夺式、非掠夺式分布式锁（区别在于是否在指定时间之后自觉拿线程锁）
- 关闭分布式锁中的log.info，QPS优化到1000左右（3000并发）

v1.9.0 2023 / 09 /08 - 2023 / 09 / 11

- 新增策略模式，选取内存策略做到内存缓存策略，有效降低缓存穿透、击穿。

- 优化分布式锁，使用消息队列监听+FIFOUNI队列保证分布式锁的公平性与可靠性，看门狗改为使用`ScheduledThreadPoolExecutor`实现，单机模式下（排除网络IO）与synchronized性能差别不大

  20个线程

  ![image-20230910194932722](http://gd.7n.cdn.wzl1.top/typora/img/image-20230910194932722.png)

  ![image-20230910194945932](http://gd.7n.cdn.wzl1.top/typora/img/image-20230910194945932.png)

  150个线程

  ![image-20230910195006933](http://gd.7n.cdn.wzl1.top/typora/img/image-20230910195006933.png)

  ![image-20230910195030253](http://gd.7n.cdn.wzl1.top/typora/img/image-20230910195030253.png)

v1.8.1 2023 / 08 / 19 / 17：24<br>
优化分布式锁：采用 自旋锁+同步锁，但是并没有解决公平竞争锁的问题，如果要解决可以使用消息队列<br>
v1.8.0  2023 / 08 / 19 / 17：16<br>
修复RedisUtil出现多个LockUtil Bean的问题<br>
v1.8.0  2023 / 08 / 06 / 04：22<br>
修改LockUtil，利用Redistemplate实现可重入分布式锁<br>
RedisUtils新增ZSet和Set类型的支持<br>
v1.7.12 修复时间 2023 / 05 / 29 / 19:02<br>
解决强制配置七牛云服务的问题<br>
v1.7.11 修复时间 2023 / 05 / 29 / 10:39<br>
解决部分类自动装配失败问题<br>
原因：@Scope("Single") -> @Scope("singleton") 可能是最后全局替换的时候替换掉了<br>
v1.7.1  更新日期 2023 / 04 / 15 / 17:37<br>
5-8:对LockConfig进行优化<br>
新增分布式锁看门狗机制，零代码侵入，解决为使用分布式锁而选择Redssion的问题<br>
新增RedisUtils，对RedisTemplate进行封装，并且实现了分布式锁功能<br>
v1.6.1  更新日期 2023 / 02 / 09 / 01:13<br>
将expDateUtil移动只cn.katool.util下<br>
新增expBase64Util，扩展于org.springframework.util.Base64Utils<br>
v1.6.0  更新日期 2023 / 01 / 29 / 02:40<br>
包从com.Karos.KaTool改为cn.katool<br>
新增KaTool异常处理类<br>
基于hutool.DateUtil扩展开发expDateUtil，支持Corn和Date间的互相转换<br>
预计近日将上传Maven中央仓库<br>
v1.5.5<br>
GenerateCode类改名为GenerateCodeUtil<br>
大部分类新增日志记录<br>
v1.5.4<br>
分布式锁优化，新增分布式锁延期<br>
v1.5.3<br>
新增函数式接口，简化lambda开发<br>
v1.5.2<br>
分布式锁优化
v1.5.1<br>
分布式锁单例模式优化
v1.5.0<br>
锁工具类-分布式锁实现，新增启动Banner<br>
**Tips:使用锁工具类必须使用自动装配，我在工具类内部实现了单例模式**<br>
v1.4.8<br>
验证码生成优化<br>
v1.4.7<br>
七牛云文件管理新增通过URL获得源文件名方法<br>
优化Starter架构<br>
临时文件生成<br>
v1.4.6<br>
支持七牛云上传设定子目录<br>
v1.4.5<br>
支持七牛云上传设置上传目录<br>
v1.4.0<br>
新增：七牛云文件存在判断、文件强制上传<br>
增强：文件上传后cdn强制刷新<br>
v1.3.5<br>
Ip工具：避免Nginx反向代理，获得真实IP<br>
Nginx-Config的server中加上<br>

```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
<br>
v1.3.0<br>
图形验证码生成<br>
V1.1.2<br>
图片与Base64互转<br>
V1.0.0<br>
七牛云<br>

## Todo
