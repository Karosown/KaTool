package cn.katool.lock;

import cn.hutool.core.util.ObjectUtil;
import cn.katool.util.ScheduledTaskUtil;
import cn.katool.util.db.nosql.RedisUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

@Component
@Slf4j
public class LockMessageWatchDog implements MessageListener {
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<Thread>> threadWaitQueue=new ConcurrentHashMap<>();;
    public static final String LOCK_MQ_NAME = "LOCK:RELASE:QUEUE";


    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("从消息通道={}监听到消息",new String(pattern));
        log.info("从消息通道={}监听到消息",new String(message.getChannel()));
        String lockName = new String(message.getBody()).substring(1, message.getBody().length - 1);
        log.info("元消息={}", lockName);
        log.info("threadWaitQueue:{}",threadWaitQueue);;
        ConcurrentLinkedQueue<Thread> threads = threadWaitQueue.get(lockName);
        if (ObjectUtil.isEmpty(threads)||threads.isEmpty()){
            log.info("没有线程需要lock:{}在等待", lockName);
            threadWaitQueue.remove(lockName);
            return ;
        }
        Thread poll = threads.poll();
        log.info("唤醒线程={}",poll.getName());
        LockSupport.unpark(poll);
    }

    //表示监听一个频道

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(this, new ChannelTopic(LOCK_MQ_NAME));
        return container;
    }


}
