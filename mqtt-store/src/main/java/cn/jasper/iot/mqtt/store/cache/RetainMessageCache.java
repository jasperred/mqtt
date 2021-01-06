package cn.jasper.iot.mqtt.store.cache;

import cn.hutool.core.util.StrUtil;
import cn.jasper.iot.mqtt.common.message.RetainMessageStore;
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

    public List<RetainMessageStore> search(String topic) {
        if (StrUtil.contains(topic, '#') || StrUtil.contains(topic, '+')){
            return all( topic);
        }else {
            List<String> rl = cacheService.search(CACHE_PRE + topic);
            if(rl==null){
                return null;
            }
            List<RetainMessageStore> list = new ArrayList<>();
            rl.forEach(str->{
                list.add(JSONObject.parseObject(str, RetainMessageStore.class));
            });
            return list;
        }
    }
    public List<RetainMessageStore> all(String topic) {
        List<String> rm = cacheService.searchKey(CACHE_PRE);
        if(rm==null||rm.isEmpty()){
            return null;
        }
        List<RetainMessageStore> rl = new ArrayList<>();
        rm.forEach((topicFilter) -> {
            topicFilter = topicFilter.substring(CACHE_PRE.length());
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = StrUtil.split(topic, '/');//a
                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');//#
                String newTopicFilter = "";
                for (int i = 0; i < spliteTopicFilters.size(); i++) {
                    String value = splitTopics.get(i);
                    if (value.equals("+")) {
                        newTopicFilter = newTopicFilter + "+/";
                    } else if (value.equals("#")) {
                        newTopicFilter = newTopicFilter + "#/";
                        break;
                    } else {
                        newTopicFilter = newTopicFilter + spliteTopicFilters.get(i) + "/";
                    }
                }
                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                if (topic.equals(newTopicFilter)) {
                    rl.add(JSONObject.parseObject(cacheService.get(CACHE_PRE+topicFilter), RetainMessageStore.class));
                }
            }
        });
        return rl;
    }
}
