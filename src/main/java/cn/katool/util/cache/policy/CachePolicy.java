package cn.katool.util.cache.policy;

public interface CachePolicy {

    Object  get(String key);

    void    set(String key, Object value);

    void update(String key, Object value);

    void setOrUpdate(String key, Object value);

    void    remove(String key);

    void    clear();

    // 获取缓存大小
    Long size();
}
