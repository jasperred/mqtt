package cn.jasper.iot.mqtt.store.cache;

import cn.jasper.iot.mqtt.common.message.RetainMessageStore;
import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 保留消息
 */
@Service
public class RetainMessageCache {
    private final static String CACHE_PRE = "mqtt:retain:";
    @Autowired
    private CacheService cacheService;

    public RetainMessageStore put(String topic, RetainMessageStore obj) {
        cacheService.put(CACHE_PRE + topic, JSONObject.toJSONString(obj));
        return obj;
    }

    public RetainMessageStore get(String topic) {
        return JSONObject.parseObject(cacheService.get(CACHE_PRE + topic), RetainMessageStore.class);
    }

    public boolean containsKey(String topic) {
        return cacheService.containsKey(CACHE_PRE + topic);
    }

    @Async
    public void remove(String topic) {
        cacheService.remove(CACHE_PRE + topic);
    }

    public List<RetainMessageStore> all(String topic) {
        List<String> rl = cacheService.search(CACHE_PRE + topic);
        if(rl==null||rl.size()==0){
            return null;
        }
        List<RetainMessageStore> list = new ArrayList<>();
        rl.forEach(str->{
            list.add(JSONObject.parseObject(str, RetainMessageStore.class));
        });
        return list;
    }
}
