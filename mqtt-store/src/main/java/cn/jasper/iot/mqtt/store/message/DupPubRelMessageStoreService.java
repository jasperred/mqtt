/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.message;

import cn.jasper.iot.mqtt.common.message.DupPubRelMessageStore;
import cn.jasper.iot.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IMessageIdService;
import cn.jasper.iot.mqtt.store.cache.DupPubRelMessageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PubRel重发
 */
@Service
public class DupPubRelMessageStoreService implements IDupPubRelMessageStoreService {

    @Autowired
    private IMessageIdService messageIdService;

    @Autowired
    private DupPubRelMessageCache dupPubRelMessageCache;

    @Override
    public void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore) {
        dupPubRelMessageCache.put(clientId, dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
    }

    @Override
    public List<DupPubRelMessageStore> get(String clientId) {
        return dupPubRelMessageCache.get(clientId);
    }

    @Override
    public void remove(String clientId, int messageId) {
        dupPubRelMessageCache.remove(clientId, messageId);
    }

    @Override
    public void removeByClient(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            dupPubRelMessageCache.remove(clientId);
        }
    }
}
