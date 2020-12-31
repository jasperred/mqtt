package cn.jasper.iot.mqtt.store.cache;

import cn.jasper.iot.mqtt.common.message.DupPublishMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 发送消息
 */
@Service
public class DupPublishMessageCache {
    private final static String CACHE_PRE = "mqtt:publish:";
    @Autowired
    private CacheService cacheService;

    public DupPublishMessageStore put(String clientId, Integer messageId, DupPublishMessageStore dupPublishMessageStore) {
        cacheService.put(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPublishMessageStore));
        return dupPublishMessageStore;
    }

    public List<DupPublishMessageStore> get(String clientId) {
        List<String> rl = cacheService.getForList(clientId);
        if(rl==null||rl.size()==0){
            return null;
        }
        List<DupPublishMessageStore> list = new ArrayList<>();
        rl.forEach(str->{
            list.add(JSONObject.parseObject(str, DupPublishMessageStore.class));
        });
        return list;
    }

    public boolean containsKey(String clientId) {
        return cacheService.containsKey(CACHE_PRE + clientId);
    }

    @Async
    public void remove(String clientId, Integer messageId) {
        cacheService.remove(CACHE_PRE + clientId, String.valueOf(messageId));
    }

    @Async
    public void remove(String clientId) {
        cacheService.remove(CACHE_PRE + clientId);
    }
}
