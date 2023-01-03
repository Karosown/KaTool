/**
 * Title
 *
 * @ClassName: LockUtil
 * @Description:锁工具类,通过内部枚举类实现单例，防止反射攻击
 * @author: Karos
 * @date: 2023/1/4 0:17
 * @Blog: https://www.wzl1.top/
 */

package com.karos.KaTool.lock;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class LockUtil {
        @Resource
        RedisTemplate redisTemplate;
        //加锁
        public boolean DistributedLock(Object obj,Long exptime,TimeUnit timeUnit){
                Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("Lock:"+obj.toString(), "1", exptime, timeUnit);
                return BooleanUtil.isTrue(aBoolean);
        }

        //检锁
        public void DistributedAssert(Object obj){
                while(true){
                        Object o = redisTemplate.opsForValue().get("Lock:" + obj.toString());
                        if (ObjectUtils.isEmpty(o))return;
                }
        }

        //释放锁
        public boolean DistributedUnLock(Object obj){
                Boolean aBoolean = redisTemplate.delete("Lock:" + obj.toString());
                return BooleanUtil.isTrue(aBoolean);
        }



        //利用枚举类实现单例模式，枚举类属性为静态的
        private enum SingletonFactory{
                Singleton;
                LockUtil lockUtil;
                private SingletonFactory(){
                        lockUtil=new LockUtil();
                }
                public LockUtil getInstance(){
                        return lockUtil;
                }
        }

        @Bean
        public LockUtil getInstance(){
                return SingletonFactory.Singleton.lockUtil;
        }
}
