package cn.jasper.iot.mqtt.store.cache;

import java.util.List;
import java.util.Map;

/**
 * @description: 缓存服务
 * @author: jasper
 * @create: 2020-12-30 10:10
 */
public interface CacheService {
    /**
     * 设置过期时间
     * @param k
     * @param expire
     */
    public void expire(String k,int expire);
    /**
     * 设置Value
     * @param k
     * @param value
     * @return
     */
    public void put(String k, String value);

    /**
     * 设置Value
     * @param k1
     * @param k2
     * @param value
     * @return
     */
    public void put(String k1, String k2, String value);

    /**
     * 获得Value
     * @param k1
     * @param k2
     * @return
     */
    public String get(String k1, String k2);

    /**
     * 获得Value
     * @param k1
     * @return
     */
    public List<String> getForList(String k1);

    /**
     * 获得Value
     * @param k1
     * @return
     */
    public String get(String k1);

    /**
     * 删除Key
     * @param k1
     * @param k2
     */
    public void remove(String k1, String k2);

    /**
     * 删除Key
     * @param k1
     */
    public void remove(String k1);

    /**
     *是否包含Key
     * @param k
     * @return
     */
    public boolean containsKey(String k);

    /**
     * 是否包含Key
     * @param k1
     * @param k2
     * @return
     */
    public boolean containsKey(String k1, String k2);

    /**
     * 返回符合条件的Value
     * @param search
     * @return
     */
    public List<String> search(String search);

    /**
     * 返回符合条件的Key
     * @param key
     * @return
     */
    public List<String> searchKey(String key);

    /**
     * 返回全部Value
     * @return
     */
    public Map<String, Map<String,String>> all(String pre);

    /**
     * 变量自增
     * @param key
     * @return
     */
    public long incr(String key);
}
