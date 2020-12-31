/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.message.DupPubRelMessageStore;
import cn.jasper.iot.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREC连接处理
 */
public class PubRec {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

	private IDupPublishMessageStoreService dupPublishMessageStoreService;

	private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

	public PubRec(IDupPublishMessageStoreService dupPublishMessageStoreService, IDupPubRelMessageStoreService dupPubRelMessageStoreService) {
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
	}

	public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
		MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
		LOGGER.debug("PUBREC - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		//丢弃消息
		dupPublishMessageStoreService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		//存储PUBREC中的报文标识符
		DupPubRelMessageStore dupPubRelMessageStore = new DupPubRelMessageStore().setClientId((String) channel.attr(AttributeKey.valueOf("clientId")).get())
			.setMessageId(variableHeader.messageId());
		dupPubRelMessageStoreService.put((String) channel.attr(AttributeKey.valueOf("clientId")).get(), dupPubRelMessageStore);
		channel.writeAndFlush(pubRelMessage);
	}

}
