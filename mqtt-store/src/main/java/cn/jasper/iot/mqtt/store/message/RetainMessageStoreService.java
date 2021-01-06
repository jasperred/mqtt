/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.message;

import cn.hutool.core.util.StrUtil;
import cn.jasper.iot.mqtt.common.message.IRetainMessageStoreService;
import cn.jasper.iot.mqtt.common.message.RetainMessageStore;
import cn.jasper.iot.mqtt.store.cache.RetainMessageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 保留消息操作
 */
@Service
public class RetainMessageStoreService implements IRetainMessageStoreService {

    @Autowired
    private RetainMessageCache retainMessageCache;

    @Override
    public void put(String topic, RetainMessageStore retainMessageStore) {
        retainMessageCache.put(topic, retainMessageStore);
    }

    @Override
    public RetainMessageStore get(String topic) {
        return retainMessageCache.get(topic);
    }

    @Override
    public void remove(String topic) {
        retainMessageCache.remove(topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return retainMessageCache.containsKey(topic);
    }

    @Override
    public List<RetainMessageStore> search(String topicFilter) {
        return retainMessageCache.search(topicFilter);
    }
}
