/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IMessageIdService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBACK连接处理
 */
public class PubAck {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubAck.class);

	private IMessageIdService messageIdService;

	private IDupPublishMessageStoreService dupPublishMessageStoreService;

	public PubAck(IMessageIdService messageIdService, IDupPublishMessageStoreService dupPublishMessageStoreService) {
		this.messageIdService = messageIdService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
	}

	public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
		if(variableHeader==null){
			return;
		}
		int messageId = variableHeader.messageId();
		LOGGER.debug("PUBACK - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
		//收到ACK后丢弃发送消息
		dupPublishMessageStoreService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
		//messageIdService.releaseMessageId(messageId);
	}

}
