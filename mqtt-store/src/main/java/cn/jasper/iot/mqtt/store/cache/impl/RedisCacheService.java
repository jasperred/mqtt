package cn.jasper.iot.mqtt.store.cache.impl;

import cn.jasper.iot.mqtt.store.cache.CacheService;
import cn.jasper.iot.mqtt.store.utils.RedisUtils;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: Redis实现
 * @author: jasper
 * @create: 2020-12-30 10:36
 */
@Service
@ConditionalOnProperty(name = {"spring.mqtt.broker.cache"},havingValue = "redis",matchIfMissing = true)
public class RedisCacheService implements CacheService {
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void expire(String k, int expire) {
        redisUtils.expire(k,expire);
    }

    @Override
    public void put(String k, String value) {
        redisUtils.set(k,value);
    }

    @Override
    public void put(String k1, String k2, String value) {
        redisUtils.hset(k1,k2,value);
    }

    @Override
    public String get(String k1, String k2) {
        return redisUtils.hget(k1,k2);
    }

    @Override
    public List<String> getForList(String k1) {
        return redisUtils.hvals(k1);
    }

    @Override
    public String get(String k1) {
        return redisUtils.get(k1);
    }

    @Override
    public void remove(String k1, String k2) {
        redisUtils.hdel(k1,k2);
    }

    @Override
    public void remove(String k1) {
        redisUtils.del(k1);
    }

    @Override
    public boolean containsKey(String k) {
        return redisUtils.exists(k);
    }

    @Override
    public boolean containsKey(String k1, String k2) {
        return redisUtils.hexists(k1,k2);
    }

    @Override
    public List<String> search(String search) {
        //必须传入条件
        if(StringUtil.isNullOrEmpty(search)){
            return null;
        }
        Map<String,String> rm = redisUtils.hgetall(search);
        if(rm==null||rm.isEmpty()){
            return null;
        }
        List rl = new ArrayList();
        rl.addAll(rm.values());
        return rl;
    }

    @Override
    public Map<String, Map<String,String>> all(String pre) {
        return redisUtils.scan(pre);
    }
}
