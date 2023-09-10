package cn.katool.util.cache.policy.impl;

import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.utils.CaffeineUtils;

import javax.annotation.Resource;


public class CaffeineCachePolicy implements CachePolicy {
    @Resource
    private CaffeineUtils<String,Object>  caffeineUtils;

    @Override
    public Object get(String key) {
        return caffeineUtils.get(key);
    }

    @Override
    public void set(String key, Object value) {
        caffeineUtils.put(key,value);;
    }

    @Override
    public void update(String key, Object value) {
        caffeineUtils.update(key,value);
    }
    @Override
    public void setOrUpdate(String key, Object value) {
        if (caffeineUtils.contains(key)){
            caffeineUtils.update(key,value);
        }
        else{
            caffeineUtils.put(key,value);
        }
    }

    @Override
    public void remove(String key) {
        caffeineUtils.delete(key);
    }

    @Override
    public void clear() {
        caffeineUtils.deleteAll();
    }

    @Override
    public Long size() {
        return caffeineUtils.getCache().estimatedSize();
    }

}
