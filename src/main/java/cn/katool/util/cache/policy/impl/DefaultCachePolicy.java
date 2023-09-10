package cn.katool.util.cache.policy.impl;

import cn.katool.util.cache.policy.CachePolicy;

public class DefaultCachePolicy implements CachePolicy {
    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }

    @Override
    public void update(String key, Object value) {

    }

    @Override
    public void setOrUpdate(String key, Object value) {

    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Long size() {
        return null;
    }
}
