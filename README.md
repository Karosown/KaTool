# KaTools
一款拥有七牛云、IP工具、图片工具、图形验证码生成的Tools<br>
Tips：该Starter为个人练习使用，工具类和其他同类作品相比不全面望谅解
# Update
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


```Lombok.config
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header  X-Real-Port        $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
<br>
v1.3.0<br>
图形验证码生成<br>
V1.1.2<br>
图片与Base64互转<br>
V1.0.0<br>
七牛云