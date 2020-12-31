/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.internal;

import cn.jasper.iot.mqtt.config.BrokerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息转发，基于kafka
 */
@Service
public class InternalCommunication {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalCommunication.class);
    @Autowired
    private BrokerProperties brokerProperties;
//    @Autowired
//    private KafkaService kafkaService;
//    @Autowired
//    private RedisCluster redisCluster;

    public void internalSend(InternalMessage internalMessage) {
//        String processId = Lang.JdkTool.getProcessId("0");
//        //broker唯一标识 mqttwk.broker.id
//        internalMessage.setBrokerId(brokerProperties.getId());
//        internalMessage.setProcessId(processId);
//        //如果开启kafka消息转发
//        if (brokerProperties.getKafkaBrokerEnabled()) {
//            kafkaService.send(internalMessage);
//        }
//        //如果开启集群功能
//        if (brokerProperties.getClusterEnabled()) {
//            redisCluster.sendMessage(internalMessage);
//        }
    }
}
