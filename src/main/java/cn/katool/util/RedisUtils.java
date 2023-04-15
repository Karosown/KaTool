/**
 * Title
 *
 * @ClassName: LogUtil
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/3/23 9:31
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.util;


import cn.katool.Exception.KaToolException;
import cn.katool.lock.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
@Scope("prototype")
@Slf4j
public class RedisUtils {
    @Resource
    private RedisTemplate redisTemplate;

    private RedisUtils() {
    }
    private RedisUtils(RedisTemplate restemp) {
        setRedisTemplate(restemp);
    }
    public  RedisTemplate setRedisTemplate(RedisTemplate restemp){
        redisTemplate=restemp;
        return redisTemplate;
    }
    @Resource
    LockUtil lockUtil;
    private void expMsg(String Msg){
        if (getRedisTemplate()==null) throw new RuntimeException("请先设置RedisTemplate，RedisUtil中已有setRedistemplate()方法");
        if (Msg==null) throw new RuntimeException("RedisUtils -- 未知错误");
        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        throw new RuntimeException("\t"+stackTrace[1].getClassName()+"."+stackTrace[1].getMethodName()+"()方法抛出异常 --"+Msg+"\n" +
                "\t\t\tthrow posation:\t\t"+stackTrace[2].getClassName()+"."+stackTrace[2].getMethodName()+" 第"+stackTrace[2].getLineNumber()+"行");
    }
    public boolean unlock(Object lockObj){
        if (lockObj==null){
            expMsg("没有上锁");
        }
        boolean b= false;
        try {
            b = lockUtil.DistributedUnLock(lockObj);
        } catch (KaToolException e) {
            expMsg("解锁失败");
        }
        return b;
    }
    public boolean lock(Object lockObj){
        return lockUtil.DistributedLock(lockObj);
    }
    public  RedisTemplate getRedisTemplate(){
        return redisTemplate;
    }
    public Boolean setValue(String name, Object value, Long timeOut, TimeUnit timeUnit){
        if (getRedisTemplate()==null) expMsg(null);
        redisTemplate.opsForValue().set(name,value,timeOut,timeUnit);
        if (redisTemplate.opsForValue().get(name).equals(value))return true;
        return false;
    }

    public Boolean setValue(String name, Object value){
        if (getRedisTemplate()==null) expMsg(null);
        redisTemplate.opsForValue().set(name,value);
        if (redisTemplate.opsForValue().get(name).equals(value))return true;
        return false;
    }
    public Boolean remove(String name){
        if (getRedisTemplate()==null) expMsg(null);
        Boolean delete = redisTemplate.delete(name);
        return true;
    }

    public Boolean pushMap(String mapName, Map map){
        if (getRedisTemplate()==null) expMsg(null);
        redisTemplate.opsForHash().putAll(mapName,map);
        if (redisTemplate.hasKey(mapName)){
            if (redisTemplate.opsForHash().entries(mapName).isEmpty()&& !map.isEmpty()) return false;
            return true;
        }
        return false;
    }
    public Boolean pushMap(String mapName,String key,String value){
        if (getRedisTemplate()==null) expMsg(null);
        redisTemplate.opsForHash().put(mapName,key,value);
        if (redisTemplate.opsForHash().hasKey(mapName,key)){
            if (redisTemplate.opsForHash().get(mapName,key).equals(value)){
                return true;
            }
        }
        return false;
    }

    public Map getMap(String mapName){
        if (getRedisTemplate()==null) expMsg(null);
        if (redisTemplate.hasKey(mapName)){
            return redisTemplate.opsForHash().entries(mapName);
        }
        return null;
    }

    public Object getValue(String name){
        if (getRedisTemplate() == null) expMsg(null);
        return redisTemplate.opsForValue().get(name);
    }
    public Object getMap(String mapName,String key){
        if (getRedisTemplate()==null) expMsg(null);
        return redisTemplate.opsForHash().get(mapName, key);
    }
    public List getList(String listName){
        if (getRedisTemplate()==null) expMsg(null);
       return redisTemplate.opsForList().range(listName,0,-1);
    }
    public Boolean pushList(String listName,Object object){
        if (getRedisTemplate()==null) expMsg(null);
        Long oldSize = redisTemplate.opsForList().size(listName);
        if (object.getClass().isArray()){
            expMsg("该方法不能够传入数组");
        }
        if (object instanceof Collection< ? >){
            redisTemplate.opsForList().rightPushAll(listName,object);
        }
        else redisTemplate.opsForList().rightPush(listName,object);
        Long newSize = redisTemplate.opsForList().size(listName);
        //乐观锁
        if (newSize>oldSize) return true;
        return false;
    }
    public Boolean pushListLeft(String listName,Object object){
        if (getRedisTemplate()==null) expMsg(null);
        Long oldSize = redisTemplate.opsForList().size(listName);
        if (object instanceof Collection< ? >){
            redisTemplate.opsForList().leftPushAll(listName,object);
        }
        else redisTemplate.opsForList().leftPush(listName,object);;
        Long newSize = redisTemplate.opsForList().size(listName);
        //乐观锁
        if (newSize>oldSize) return true;
        return false;
    }

    //利用枚举类实现单例模式，枚举类属性为静态的
    private enum SingletonFactory{
        Singleton;
        RedisUtils redisUtils;
        private SingletonFactory(){
            redisUtils=new RedisUtils();
        }
        public RedisUtils getInstance(){
            return redisUtils;
        }
    }
    @Bean(name = "RedisUtils")
    public static RedisUtils getInstance(){
        return SingletonFactory.Singleton.getInstance();
    }
    public static RedisUtils getInstance(RedisTemplate redisTemplate){
        SingletonFactory.Singleton.redisUtils.setRedisTemplate(redisTemplate);
        return SingletonFactory.Singleton.getInstance();
    }
}
