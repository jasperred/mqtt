package cn.jasper.iot.mqtt.store.cache;

import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 无通配符订阅
 */
@Service
public class SubscribeNotWildcardCache {
    private final static String CACHE_PRE = "mqtt:subnotwildcard:";
    private final static String CACHE_CLIENT_PRE = "mqtt:client:";
    @Autowired
    private CacheService cacheService;

    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        cacheService.put(CACHE_PRE + topic, clientId, com.alibaba.fastjson.JSONObject.toJSONString(subscribeStore));
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        return com.alibaba.fastjson.JSONObject.parseObject(cacheService.get(CACHE_PRE + topic, clientId), SubscribeStore.class);
    }

    public boolean containsKey(String topic, String clientId) {
        return cacheService.containsKey(CACHE_PRE + topic, clientId);
    }

    @Async
    public void remove(String topic, String clientId) {
        cacheService.remove(CACHE_CLIENT_PRE + clientId, topic);
    }

    @Async
    public void remove(String clientId) {
        cacheService.remove(CACHE_CLIENT_PRE + clientId);
    }
    public List<SubscribeStore> all(String topic) {
        List<String> rl = cacheService.search(CACHE_PRE + topic);
        if(rl==null||rl.size()==0){
            return null;
        }
        List<SubscribeStore> list = new ArrayList<>();
        rl.forEach(str->{
            list.add(JSONObject.parseObject(str, SubscribeStore.class));
        });
        return list;
    }
}
