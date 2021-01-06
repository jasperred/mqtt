/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.message.*;
import cn.jasper.iot.mqtt.common.session.ISessionStoreService;
import cn.jasper.iot.mqtt.common.subscripe.ISubscribeStoreService;
import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import cn.jasper.iot.mqtt.internal.InternalCommunication;
import cn.jasper.iot.mqtt.store.cache.ChannelCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * PUBLISH连接处理
 */
public class Publish {

	private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);

	private ISessionStoreService sessionStoreService;

	private ISubscribeStoreService subscribeStoreService;

	private IMessageIdService messageIdService;

	private IRetainMessageStoreService retainMessageStoreService;

	private IDupPublishMessageStoreService dupPublishMessageStoreService;

	private InternalCommunication internalCommunication;
	private ChannelCache channelCache;

	public Publish(ISessionStoreService sessionStoreService, ISubscribeStoreService subscribeStoreService, IMessageIdService messageIdService, IRetainMessageStoreService retainMessageStoreService, IDupPublishMessageStoreService dupPublishMessageStoreService, InternalCommunication internalCommunication, ChannelCache channelCache) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.messageIdService = messageIdService;
		this.retainMessageStoreService = retainMessageStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.internalCommunication = internalCommunication;
		this.channelCache = channelCache;
	}

	public void processPublish(Channel channel, MqttPublishMessage msg) {
		byte[] messageBytes = new byte[msg.payload().readableBytes()];
		msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
		this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
//			InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
//				.setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
//				.setDup(false).setRetain(false);
//			internalCommunication.internalSend(internalMessage);
		// QoS=0
		if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
			//无响应报文
		}
		// QoS=1
		if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			//发布确认消息
			this.sendPubAckMessage(channel, msg.variableHeader().packetId());
		}
		// QoS=2
		//QoS2有两种处理方式，A先存储消息等收到PUBREL再进行分发，B存储报文标识，直接分发消息，收到PUBREL再丢弃报文标识
		//此处采用B方式
		if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
			//发布收到消息
			this.sendPubRecMessage(channel, msg.variableHeader().packetId());
		}
		// retain=1, 保留消息
		if (msg.fixedHeader().isRetain()) {
			//如果客户端发给服务端的PUBLISH报文的保留（RETAIN）标志被设置为1，服务端必须存储这个应用消息和它的服务质量等级（QoS），以便它可以被分发给未来的主题名匹配的订阅者
			if (messageBytes.length == 0) {
				//服务端不能存储零字节的保留消息
				retainMessageStoreService.remove(msg.variableHeader().topicName());
			} else {
				RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(msg.variableHeader().topicName()).setMqttQoS(msg.fixedHeader().qosLevel().value())
					.setMessageBytes(messageBytes);
				retainMessageStoreService.put(msg.variableHeader().topicName(), retainMessageStore);
			}
		}
	}

	private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
		//将消息发布给订阅的客户端
		List<SubscribeStore> subscribeStores = subscribeStoreService.search(topic);
		if(subscribeStores==null||subscribeStores.size()==0){
			return;
		}
		subscribeStores.forEach(subscribeStore -> {
			if (sessionStoreService.containsKey(subscribeStore.getClientId())) {
				// 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
				MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
				if (respQoS == MqttQoS.AT_MOST_ONCE) {
					writeQoS0Message(topic, messageBytes, retain, dup, subscribeStore, respQoS);
				}
				if (respQoS == MqttQoS.AT_LEAST_ONCE) {
					writeQoS12Message(topic, messageBytes, retain, dup, subscribeStore, respQoS);
				}
				if (respQoS == MqttQoS.EXACTLY_ONCE) {
					writeQoS12Message(topic, messageBytes, retain, dup, subscribeStore, respQoS);
				}
			}
		});
	}

	private void writeQoS12Message(String topic, byte[] messageBytes, boolean retain, boolean dup, SubscribeStore subscribeStore, MqttQoS respQoS) {
		int messageId = messageIdService.getNextMessageId();
		MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
				new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
		LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
		//存储报文,为了处理消息重发
		DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
				.setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
		dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
		Channel channel = channelCache.find(sessionStoreService.get(subscribeStore.getClientId()).getChannelId());
		if(channel!=null&&channel.isOpen()){
			channel.writeAndFlush(publishMessage);
		}
	}

	private void writeQoS0Message(String topic, byte[] messageBytes, boolean retain, boolean dup, SubscribeStore subscribeStore, MqttQoS respQoS) {
		MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
			new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
		LOGGER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
		Channel channel = channelCache.find(sessionStoreService.get(subscribeStore.getClientId()).getChannelId());
		if(channel!=null&&channel.isOpen())
		{
			channel.writeAndFlush(publishMessage);
		}
	}

	private void sendPubAckMessage(Channel channel, int messageId) {
		MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubAckMessage);
		LOGGER.debug("PUBACK - messageId: {}", messageId);
	}

	private void sendPubRecMessage(Channel channel, int messageId) {
		MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubRecMessage);
		LOGGER.debug("PUBREC - messageId: {}", messageId);
	}

}
