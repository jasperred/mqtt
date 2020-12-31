/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.subscribe;

import cn.hutool.core.util.StrUtil;
import cn.jasper.iot.mqtt.common.subscripe.ISubscribeStoreService;
import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import cn.jasper.iot.mqtt.store.cache.SubscribeNotWildcardCache;
import cn.jasper.iot.mqtt.store.cache.SubscribeWildcardCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 订阅存储服务
 */
@Service
public class SubscribeStoreService implements ISubscribeStoreService {

    @Autowired
    private SubscribeNotWildcardCache subscribeNotWildcardCache;

    @Autowired
    private SubscribeWildcardCache subscribeWildcardCache;

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        } else {
            subscribeNotWildcardCache.put(topicFilter, subscribeStore.getClientId(), subscribeStore);
        }
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            subscribeWildcardCache.remove(topicFilter, clientId);
        } else {
            subscribeNotWildcardCache.remove(topicFilter, clientId);
        }
    }

    @Override
    public void removeForClient(String clientId) {
        subscribeNotWildcardCache.remove(clientId);
        subscribeWildcardCache.remove(clientId);
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        List<SubscribeStore> list = subscribeNotWildcardCache.all(topic);
        if(list==null){
            list = new ArrayList<>();
        }
        if (list.size() > 0) {
            subscribeStores.addAll(list);
        }
        List<SubscribeStore> list2 = subscribeWildcardCache.all(topic);
        if(list2!=null&&list2.size()>0) {
            subscribeStores.addAll(list2);
        }
//        subscribeWildcardCache.all().forEach((topicFilter, map) -> {
//            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
//                List<String> splitTopics = StrUtil.split(topic, '/');//a
//                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');//#
//                String newTopicFilter = "";
//                for (int i = 0; i < spliteTopicFilters.size(); i++) {
//                    String value = spliteTopicFilters.get(i);
//                    if (value.equals("+")) {
//                        newTopicFilter = newTopicFilter + "+/";
//                    } else if (value.equals("#")) {
//                        newTopicFilter = newTopicFilter + "#/";
//                        break;
//                    } else {
//                        newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
//                    }
//                }
//                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
//                if (topicFilter.equals(newTopicFilter)) {
//                    Collection<SubscribeStore> collection = map.values();
//                    List<SubscribeStore> list2 = new ArrayList<SubscribeStore>(collection);
//                    subscribeStores.addAll(list2);
//                }
//            }
//        });
        return subscribeStores;
    }

}
