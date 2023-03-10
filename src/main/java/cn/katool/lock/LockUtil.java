/**
 * Title
 *
 * @ClassName: LockUtil
 * @Description:锁工具类,通过内部枚举类实现单例，防止反射攻击
 * @author: Karos
 * @date: 2023/1/4 0:17
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.lock;

import cn.hutool.core.util.BooleanUtil;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class LockUtil {
        @Resource
        RedisTemplate redisTemplate;
        private LockUtil(){

        }
        //加锁
        public boolean DistributedLock(Object obj,Long exptime,TimeUnit timeUnit) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                //线程被锁住了，就一直等待
                DistributedAssert(obj);
                Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("Lock:"+obj.toString(), "1", exptime, timeUnit);
                log.info("katool=> LockUntil => DistributedLock:{} value:{} extime:{} timeUnit:{}",obj.toString(), "1", exptime, timeUnit);
                return BooleanUtil.isTrue(aBoolean);
        }

        //检锁
        public void DistributedAssert(Object obj) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                while(true){
                        Object o = redisTemplate.opsForValue().get("Lock:" + obj.toString());
                        if (ObjectUtils.isEmpty(o))return;
                }
        }

        //延期
        public boolean delayDistributedLock(Object obj,Long exptime,TimeUnit timeUnit) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                Boolean aBoolean = redisTemplate.opsForValue().setIfPresent("Lock:"+obj.toString(), "1", exptime, timeUnit);
                log.info("katool=> LockUntil => delayDistributedLock:{} value:{} extime:{} timeUnit:{}",obj.toString(), "1", exptime, timeUnit);
                return BooleanUtil.isTrue(aBoolean);
        }
        //释放锁
        public boolean DistributedUnLock(Object obj) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                Boolean aBoolean = redisTemplate.delete("Lock:" + obj.toString());
                log.info("katool=> LockUntil => unDistributedLock:{} isdelete:{} ",obj.toString(),true);
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
        public static LockUtil getInstance(){
                return SingletonFactory.Singleton.lockUtil;
        }
}
