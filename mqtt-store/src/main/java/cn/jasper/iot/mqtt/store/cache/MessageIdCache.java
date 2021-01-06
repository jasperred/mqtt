package cn.jasper.iot.mqtt.store.cache;

import cn.jasper.iot.mqtt.store.message.MessageIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: 消息ID生成
 * @author: jasper
 * @create: 2021-01-06 10:46
 */
@Service
public class MessageIdCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdCache.class);

    private final static String CACHE_PRE = "mqtt:messageid:num";
    @Autowired
    private CacheService cacheService;

    public void cleanMessageId(){
        cacheService.remove(CACHE_PRE);
    }

    public int getNextMessageId(){
        try {
            while (true) {
                int nextMsgId = (int) (cacheService.incr(CACHE_PRE) % 65536);
                if (nextMsgId > 0) {
                    return nextMsgId;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }
}
