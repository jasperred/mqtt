/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREL连接处理
 */
public class PubRel {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);
	private IDupPublishMessageStoreService dupPublishMessageStoreService;
	public PubRel(IDupPublishMessageStoreService dupPublishMessageStoreService){
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
	}

	public void processPubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {
		MqttMessage pubCompMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
		LOGGER.debug("PUBREL - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		//丢弃报文标识符
		dupPublishMessageStoreService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		channel.writeAndFlush(pubCompMessage);
	}

}
