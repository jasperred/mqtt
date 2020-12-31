package cn.jasper.iot.mqtt.store.cache;

import cn.hutool.core.util.StrUtil;
import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 带通配符的订阅
 */
@Service
public class SubscribeWildcardCache {
    private final static String CACHE_PRE = "mqtt:subwildcard:";
    private final static String CACHE_CLIENT_PRE = "mqtt:client:";
    @Autowired
    private CacheService cacheService;

    public SubscribeStore put(String topic, String clientId, SubscribeStore subscribeStore) {
        cacheService.put(CACHE_PRE + topic, clientId, JSONObject.toJSONString(subscribeStore));
        return subscribeStore;
    }

    public SubscribeStore get(String topic, String clientId) {
        return JSONObject.parseObject(cacheService.get(CACHE_PRE + topic, clientId), SubscribeStore.class);
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
        Map<String,Map<String,String>> rm = cacheService.all(CACHE_PRE);
        if(rm==null||rm.isEmpty()){
            return null;
        }
        List<SubscribeStore> rl = new ArrayList<>();
        rm.forEach((topicFilter, map) -> {
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = StrUtil.split(topic, '/');//a
                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');//#
                String newTopicFilter = "";
                for (int i = 0; i < spliteTopicFilters.size(); i++) {
                    String value = spliteTopicFilters.get(i);
                    if (value.equals("+")) {
                        newTopicFilter = newTopicFilter + "+/";
                    } else if (value.equals("#")) {
                        newTopicFilter = newTopicFilter + "#/";
                        break;
                    } else {
                        newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                    }
                }
                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                if (topicFilter.equals(newTopicFilter)) {
                    Collection<String> collection = map.values();
                    List<SubscribeStore> list2 = new ArrayList<SubscribeStore>();
                    collection.forEach(str->{
                        list2.add(JSONObject.parseObject(str, SubscribeStore.class));
                    });
                    rl.addAll(list2);
                }
            }
        });
        return rl;
    }
}
