/**
 * Title
 *
 * @ClassName: LogUtil
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/3/23 9:31
 * @Blog: https://www.wzl1.top/
 */

package cn.katool.util.db.nosql;


import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;
import cn.katool.lock.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        gaveRedisTemplate(restemp);
    }
    public  RedisTemplate gaveRedisTemplate(RedisTemplate restemp){
        redisTemplate=restemp;
        return redisTemplate;
    }
    @Resource(name = "LockUtil")
    LockUtil lockUtil;
    private void expMsg(String Msg){
        if (obtainRedisTemplate()==null) {
            throw new RuntimeException("请先设置RedisTemplate，RedisUtil中已有setRedistemplate()方法");
        }
        if (Msg==null) {
            throw new RuntimeException("RedisUtils -- 未知错误");
        }
        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        throw new RuntimeException("\t"+stackTrace[1].getClassName()+"."+stackTrace[1].getMethodName()+"()方法抛出异常 --"+Msg+"\n" +
                "\t\t\tthrow posation:\t\t"+stackTrace[2].getClassName()+"."+stackTrace[2].getMethodName()+" 第"+stackTrace[2].getLineNumber()+"行");
    }
    public boolean unlock(Object lockObj){
        if (lockObj==null){
            expMsg("没有上锁");
        }
        Long b=-1L;
        try {
            b = lockUtil.DistributedUnLock(lockObj);
        } catch (KaToolException e) {
            e.printStackTrace();
        }
        if (b==null) {
            return false;
        }
        return (b+1)<=1;//防止出现精度丢失问题
    }
    public boolean lock(Object lockObj){
        return lockUtil.DistributedLock(lockObj,false);
    }

    public boolean lock(Object lockObj,Boolean isAgress){
        return lockUtil.DistributedLock(lockObj,isAgress);
    }
    public  RedisTemplate obtainRedisTemplate(){
        return redisTemplate;
    }
    public Boolean setValue(String key, Object value, Long timeOut, TimeUnit timeUnit){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForValue().set(key,value,timeOut,timeUnit);
        if (redisTemplate.opsForValue().get(key).equals(value)) {
            return true;
        }
        return false;
    }

    public Boolean setValue(String key, Object value){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForValue().set(key,value);
        if (redisTemplate.opsForValue().get(key).equals(value)) {
            return true;
        }
        return false;
    }
    public Boolean remove(String key){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        Boolean delete = redisTemplate.delete(key);
        return delete;
    }

    public Set getZset(String hashKey){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        BoundZSetOperations boundZSetOperations = redisTemplate.boundZSetOps(hashKey);
        Set range = boundZSetOperations.range(0, boundZSetOperations.size());
        return range;
    }
    public Set<ZSetOperations.TypedTuple> getZsetWithScores(String hashKey){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        BoundZSetOperations boundZSetOperations = redisTemplate.boundZSetOps(hashKey);
        Set<ZSetOperations.TypedTuple> range = boundZSetOperations.rangeWithScores(0, boundZSetOperations.size());
        return range;
    }
    public Boolean putZset(String hashKey,Object value,Double score){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForZSet().add(hashKey, value,score);
        if (!redisTemplate.hasKey(hashKey)) {
            return false;
        }
        Double isScore = redisTemplate.opsForZSet().score(hashKey, value);
        if (score.equals(isScore)) {
            return true;
        }
        return false;
    }

    public Boolean putZset(String hashKey,Set<ZSetOperations.TypedTuple> set){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForZSet().add(hashKey,set);
        if (!redisTemplate.hasKey(hashKey)) {
            return false;
        }
        ArrayList<Object> values=new ArrayList<>();
        ArrayList<Object> scores=new ArrayList<>();
        set.forEach(v->{
           values.add(v.getValue());
           scores.add(v.getScore());
        });
        List score = redisTemplate.opsForZSet().score(hashKey, values.toArray());
        if (scores.equals(score)) {
            return true;
        }
        return false;
    }

    public Set getSet(String hashKey){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        return redisTemplate.boundSetOps(hashKey).members();
    }
    public Boolean putSet(String hashKey,Object value){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        BoundSetOperations boundSetOperations = redisTemplate.boundSetOps(hashKey);
        boundSetOperations.add(value);
        if (!redisTemplate.hasKey(hashKey)) {
            return false;
        }
        if (boundSetOperations.isMember(value)){
                return true;
        }
        return false;
    }
    
    
    public Boolean putSet(String hashKey, Set set){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        Object[] objects = set.toArray();
        redisTemplate.opsForSet().add(hashKey,objects);
        Map<Object,Boolean> member = redisTemplate.opsForSet().isMember(hashKey, objects);
        AtomicReference<Boolean> isOk= new AtomicReference<>(true);
        member.forEach((k,v)->{
            if (!isOk.get()) {
                return;
            }
            if (!v){
                isOk.set(false);
            }
        });
        return isOk.get();
    }
    public Boolean putSet(String hashKey,Object... value){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForSet().add(hashKey,value);
        Map<Object,Boolean> member = redisTemplate.opsForSet().isMember(hashKey, value);
        AtomicReference<Boolean> isOk= new AtomicReference<>(true);
        member.forEach((k,v)->{
            if (!isOk.get()) {
                return;
            }
            if (!v){
                isOk.set(false);
            }
        });
        return isOk.get();
    }
    public Boolean pushMap(String hashKey, Map map){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForHash().putAll(hashKey,map);
        if (redisTemplate.hasKey(hashKey)){
            if (redisTemplate.opsForHash().entries(hashKey).isEmpty()&& !map.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }
    public Boolean pushMap(String hashKey,Object key,Object value){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        redisTemplate.opsForHash().put(hashKey,key,value);
        if (redisTemplate.opsForHash().hasKey(hashKey,key)){
            if (redisTemplate.opsForHash().get(hashKey,key).equals(value)){
                return true;
            }
        }
        return false;
    }

    public Map getMap(String hashKey){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        if (redisTemplate.hasKey(hashKey)){
            return redisTemplate.opsForHash().entries(hashKey);
        }
        return null;
    }

    public Object getValue(String hashKey){
        if (obtainRedisTemplate() == null) {
            expMsg(null);
        }
        return redisTemplate.opsForValue().get(hashKey);
    }
    public Object getMap(String hashKey,Object key){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        return redisTemplate.opsForHash().get(hashKey, key);
    }
    public List getList(String hashKey){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
       return redisTemplate.opsForList().range(hashKey,0,-1);
    }
    public Boolean pushList(String hashKey,Object object){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        Long oldSize = redisTemplate.opsForList().size(hashKey);
        if (object.getClass().isArray()){
            expMsg("该方法不能够传入数组");
        }
        if (object instanceof Collection< ? >){
            redisTemplate.opsForList().rightPushAll(hashKey,object);
        }
        else {
            redisTemplate.opsForList().rightPush(hashKey,object);
        }
        Long newSize = redisTemplate.opsForList().size(hashKey);
        //乐观锁
        if (newSize>oldSize) {
            return true;
        }
        return false;
    }
    public Boolean pushListLeft(String hashKey,Object object){
        if (obtainRedisTemplate()==null) {
            expMsg(null);
        }
        Long oldSize = redisTemplate.opsForList().size(hashKey);
        if (object instanceof Collection< ? >){
            redisTemplate.opsForList().leftPushAll(hashKey,object);
        }
        else {
            redisTemplate.opsForList().leftPush(hashKey,object);
        }
        ;
        Long newSize = redisTemplate.opsForList().size(hashKey);
        //乐观锁
        if (newSize>oldSize) {
            return true;
        }
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
        return SingletonFactory.Singleton.getInstance();
    }
}
