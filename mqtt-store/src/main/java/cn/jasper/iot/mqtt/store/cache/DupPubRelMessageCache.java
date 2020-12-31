package cn.jasper.iot.mqtt.store.cache;

import cn.jasper.iot.mqtt.common.message.DupPubRelMessageStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Rel消息
 */
@Service
public class DupPubRelMessageCache {
    private final static String CACHE_PRE = "mqtt:pubrel:";
    @Autowired
    private CacheService cacheService;

    public DupPubRelMessageStore put(String clientId, Integer messageId, DupPubRelMessageStore dupPubRelMessageStore) {
        cacheService.put(CACHE_PRE + clientId, String.valueOf(messageId), JSONObject.toJSONString(dupPubRelMessageStore));
        return dupPubRelMessageStore;
    }

    public List<DupPubRelMessageStore> get(String clientId) {
        List<String> rl = cacheService.getForList(clientId);
        if(rl==null||rl.size()==0){
            return null;
        }
        List<DupPubRelMessageStore> list = new ArrayList<>();
        rl.forEach(str->{
            list.add(JSONObject.parseObject(str, DupPubRelMessageStore.class));
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
