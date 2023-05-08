<img src="http://7n.cdn.wzl1.top/typora/img/KaTool.png" alt="KaTool" style="zoom:200%;" />

&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;![SpringBoot-2.7.0](https://img.shields.io/badge/SpringBoot-2.7.0-green)&emsp;&emsp;&emsp;![Hutool-5.8.10](https://img.shields.io/badge/Hutool-5.8.10-green)

# KaTool (卡托)
KaTool - 一款拥有七牛云文件处理、IP工具、IO工具、图形验证码生成、随机验证码生成、Date工具、Base64工具、函数式接口、分布式锁实现的Java开发工具类，依赖于SpringBoot框架<br>

**Tips：该Starter为个人项目使用，Starter制作仅满足于个人目前的开发需求，工具类和其他同类作品相比不全面望谅解，在图片写入OutputStream时，使用到了Hutool**
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
    <version>{{KaTool.version}}</version>
</dependency>
```
## Application.yml配置说明
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
    internalLockLeaseTime: 30 # 上锁最少时间，默认为30L
    timeUnit: seconds #单位 秒
```
## Nginx配置
Nginx反向代理后获取真实来源IP
```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
## Update
v1.7.1  更新日期 2023 / 04 / 15 / 17:37<br>
5-8:对LockConfig进行优化
新增分布式锁看门狗机制，零代码侵入，解决为使用分布式锁而选择Redssion的问题
新增RedisUtils，对RedisTemplate进行封装，并且实现了分布式锁功能
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
