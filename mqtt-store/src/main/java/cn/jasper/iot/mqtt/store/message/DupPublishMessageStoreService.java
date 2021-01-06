/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.message;

import cn.jasper.iot.mqtt.common.message.DupPublishMessageStore;
import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IMessageIdService;
import cn.jasper.iot.mqtt.store.cache.DupPublishMessageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Publish重发
 */
@Service
public class DupPublishMessageStoreService implements IDupPublishMessageStoreService {

    @Autowired
    private IMessageIdService messageIdService;
    @Autowired
    private DupPublishMessageCache dupPublishMessageCache;

    @Override
    public void put(String clientId, DupPublishMessageStore dupPublishMessageStore) {
        dupPublishMessageCache.put(clientId, dupPublishMessageStore.getMessageId(), dupPublishMessageStore);
    }

    @Override
    public List<DupPublishMessageStore> get(String clientId) {
        return dupPublishMessageCache.get(clientId);
    }

    @Override
    public void remove(String clientId, int messageId) {
        dupPublishMessageCache.remove(clientId, messageId);
    }

    @Override
    public void removeByClient(String clientId) {
        dupPublishMessageCache.remove(clientId);
    }
}
