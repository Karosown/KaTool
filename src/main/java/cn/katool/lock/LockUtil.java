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

import ch.qos.logback.core.util.TimeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.katool.Config.LockConfig;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.other.MethodIntefaceUtil;
import com.qiniu.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class LockUtil {
        @Resource
        RedisTemplate redisTemplate;

        @Resource(name = "getLockScript")
        private String lockScript;
        @Resource(name = "getUnLockScript")
        private String unLockScript;

        public String serviceUUid=RandomUtil.randomString(16);

        private static boolean isOpenCorn=false;

        /**
         * 带看门狗机制上锁
         * @param lockObj
         * @return
         */
        public boolean DistributedLock(Object lockObj){
                try {
                        return DistributedLock(lockObj,null,null);
                } catch (KaToolException e) {
                        throw new RuntimeException(e);
                }
        }
        @Resource
        LockConfig lockConfig;
        //加锁
        public Long luaToRedisByLock(String lockName,Long expTime,TimeUnit timeUnit){
                long id = Thread.currentThread().getId();
                //生成value1
                String hashKey= DigestUtils.md5DigestAsHex(new String(id+serviceUUid).getBytes());
                DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<String> args = new ArrayList();
                keys.add(lockName);
                keys.add(String.valueOf(TimeoutUtils.toMillis(expTime,timeUnit)));
                args.add(hashKey);
                defaultRedisScript.setScriptText(lockScript);
                defaultRedisScript.setResultType(Long.class);
                Long execute = (Long) redisTemplate.execute(defaultRedisScript, keys, args.toArray());
                return execute;
        }

        //加锁
        @SneakyThrows
        public Long luaToRedisByUnLock(String lockName){
                long id = Thread.currentThread().getId();
                //生成value1
                String hashKey= DigestUtils.md5DigestAsHex(new String(id+serviceUUid).getBytes());
                DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
                ArrayList<String> keys = new ArrayList<>();
                ArrayList<String> args = new ArrayList();
                keys.add(lockName);
                args.add(hashKey);
                defaultRedisScript.setScriptText(unLockScript);
                defaultRedisScript.setResultType(Long.class);
                Long remainLocks = (Long) redisTemplate.execute(defaultRedisScript, keys, args.toArray());
                if (ObjectUtil.isEmpty(remainLocks)) {
                        throw new KaToolException(ErrorCode.OPER_ERROR,"操作错误，this lock doesn't created!");
                }
                return remainLocks;
        }

        /**
         * @param obj
         * @param exptime
         * @param timeUnit
         * @return
         * @throws KaToolException
         */
        public boolean DistributedLock(Object obj,Long exptime,TimeUnit timeUnit) throws KaToolException {
                if (ObjectUtil.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                Boolean isDelay=false;
                if (ObjectUtil.isAllEmpty(exptime,timeUnit)){
                        isDelay=true;
                }
                if(ObjectUtil.isEmpty(exptime)){
                        exptime= lockConfig.getInternalLockLeaseTime();;
                }
                if (ObjectUtils.isEmpty(timeUnit)){
                        timeUnit=lockConfig.getTimeUnit();
                }
                String lockName="Lock:"+obj.toString();
                Long aLong = -1L;
                //todo：这个地方的自旋锁，有点恶心，一直强行占着cpu，后面改进hh
                while(aLong!=null){
                        try {
                                aLong=luaToRedisByLock(lockName, exptime, timeUnit);
                                Thread.sleep(aLong.longValue()/3L);     // 初步改进：使用线程休眠，采用自旋锁+线程互斥
                        } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                        }
                }
                log.info("katool=> LockUntil => DistributedLock:{} value:{} extime:{} timeUnit:{}",obj.toString(), "1", exptime, timeUnit);
                //实现看门狗
                if (isDelay){
                        if (LockUtil.isOpenCorn==false){
                                //如果同一个项目之前打开过，那么先关闭，避免重复启动
                                CronUtil.stop();
                                //支持秒级别定时任务
                                CronUtil.setMatchSecond(true);
                                //定时服务启动
                                CronUtil.start();
                                LockUtil.isOpenCorn=true;
                        }
                        Thread thread = Thread.currentThread();
                        TimeUnit finalTimeUnit = timeUnit;
                        Long finalExptime = exptime;
                        class TempClass{
                                public String scheduleId;
                        }
                        final TempClass tempClass = new TempClass();
                        tempClass.scheduleId=CronUtil.schedule("0/30 * * * * ?", new Task() {
                                @SneakyThrows
                                @Override
                                public void execute() {
                                        //判断当前线程是否存活
                                        boolean alive = thread.isAlive();
                                        if (alive) {
                                                delayDistributedLock(obj, finalExptime>=3?(finalExptime / 3):finalExptime, finalTimeUnit);
                                                return;
                                        } else {
                                                if (tempClass.scheduleId==null||"".equals(tempClass.scheduleId)){
                                                        return;
                                                }
                                                CronUtil.remove(tempClass.scheduleId);
                                                DistributedUnLock(obj);
                                                return;
                                        }
                                }
                        });
                }
                return BooleanUtil.isTrue(aLong!=null);
        }
        //延期
        public boolean delayDistributedLock(Object obj,Long exptime,TimeUnit timeUnit) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
                //本身就是一句话，具备原子性，没有必要使用lua脚本
                Boolean expire = redisTemplate.expire("Lock:" + obj.toString(), exptime, timeUnit);
                log.info("katool=> LockUntil => delayDistributedLock:{} value:{} extime:{} timeUnit:{}",obj.toString(), "1", exptime, timeUnit);
                return BooleanUtil.isTrue(expire);
        }
        //释放锁
        public Long DistributedUnLock(Object obj) throws KaToolException {
                if (ObjectUtils.isEmpty(obj)){
                        throw new KaToolException(ErrorCode.PARAMS_ERROR," Lock=> 传入obj为空");
                }
//                由于这里有了可重入锁，不应该直接删除Boolean aBoolean = redisTemplate.delete("Lock:" + obj.toString());
                Long remainLocks = luaToRedisByUnLock("Lock:" + obj.toString());
                log.info("katool=> LockUntil => unDistributedLock:{} isdelete:{} ",obj.toString(),true);
                return remainLocks;
        }
        //利用枚举类实现单例模式，枚举类属性为静态的
        private static enum SingletonFactory{
                Singleton;
                LockUtil lockUtil;
                private SingletonFactory(){
                        lockUtil=new LockUtil();
                }
                public LockUtil getInstance(){
                        return lockUtil;
                }
        }
        @Bean("LockUtil")
        public static LockUtil getInstance(){
                return SingletonFactory.Singleton.lockUtil;
        }

        private LockUtil(){

        }
        @Bean(name = "getLockScript")
        private static String getLockScript(){
                return "if redis.call(\"exists\",KEYS[1]) ~= 0 then\n" +
//                "        -- 如果不是自己的锁\n" +
                        "        if redis.call(\"exists\",KEYS[1],ARGS[1]) == 0 then\n" +
//                "            -- 不是自己的锁\n" +
                        "            return redis.call(\"pttl\",KEY[1]);\n" +
                        "        end\n" +
//                "        -- 如果是自己的锁就记录次数\n" +
                        "        redis.call(\"hincrby\",KEY[1],ARGS[1],1);\n" +
//                "        -- 延期\n" +
                        "        redis.call(\"pexpire\",KEY[1],ARGS[2]);\n" +
                        "    else\n" +
                        "        redis.call(\"hset\",KEYS[1],ARGS[1],1);\n" +
//                "        -- 设置默认延期\n" +
                        "        redis.call(\"pexpire\",KEY[1],ARGS[2]);\n" +
                        "    end\n" +
//                "    -- 如果Lock不存在，那么就直接加上就可以了，hhh\n" +
                        "    return nil;";
        }
        @Bean(name = "getUnLockScript")
        private static String getUnLockScript(){
                return "    if redis.call(\"exists\",KEYS[1]) ~= 0 then\n" +
//                "        -- 如果是自己的锁\n" +
                        "        if redis.call(\"hexists\",KEYS[1],ARGS[1]) ~= 0 then\n" +
//                "            -- 如果是最后一层 直接delete\n" +
                        "            if redis.call(\"hget\",KEYS[1],ARGS[1]) == 0 then\n" +
                        "                redis.call(\"del\",KEYs[1]);\n" +
                        "            else\n" +
                        "                redis.call(\"hincrby\",KEY[1],ARGS[1],-1);\n" +
                        "            end\n" +
                        "        end\n" +
                        "    end\n" +
//                "    -- 如果Lock不存在，那么就return，hhh\n" +
                        "    return nil;";
        }
}
