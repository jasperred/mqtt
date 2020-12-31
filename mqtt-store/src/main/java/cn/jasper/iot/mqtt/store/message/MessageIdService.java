package cn.jasper.iot.mqtt.store.message;

import cn.jasper.iot.mqtt.common.message.IMessageIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wizzer on 2018
 */
@Service
public class MessageIdService implements IMessageIdService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdService.class);

//    @Autowired
//    private RedisService redisService;

    @Override
    public int getNextMessageId() {
//        try {
//            while (true) {
//                int nextMsgId = (int) (redisService.incr("mqttwk:messageid:num") % 65536);
//                if (nextMsgId > 0) {
//                    return nextMsgId;
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        }
        return 0;
    }

    @Override
    public void releaseMessageId(int messageId) {

    }

    /**
     * 每次重启的时候初始化
     */
    public void init() {
//        redisService.del("mqttwk:messageid:num");
    }
}
