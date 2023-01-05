<img src="http://7n.cdn.wzl1.top/typora/img/KaTool.png" alt="KaTool" style="zoom:200%;" />

&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;![SpringBoot-2.7.0](https://img.shields.io/badge/SpringBoot-2.7.0-green)&emsp;&emsp;&emsp;![Hutool-5.8.10](https://img.shields.io/badge/Hutool-5.8.10-green)

# KaTool (卡托)
KaTool (卡托) - 一款拥有七牛云文件处理、分布式锁、七牛云存储管理、IP工具、IO工具、图形验证码生成、随机验证码生成的Tool<br>

**Tips：该Starter为个人项目使用，Starter制作仅满足于个人目前的开发需求，工具类和其他同类作品相比不全面望谅解，在图片写入OutputStream时，使用到了Hutool**
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
```
## Nginx配置
Nginx反向代理后获取真实来源IP
```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
## Update
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
