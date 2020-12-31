/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.auth.IAuthService;
import cn.jasper.iot.mqtt.common.message.DupPubRelMessageStore;
import cn.jasper.iot.mqtt.common.message.DupPublishMessageStore;
import cn.jasper.iot.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import cn.jasper.iot.mqtt.common.session.ISessionStoreService;
import cn.jasper.iot.mqtt.common.session.SessionStore;
import cn.jasper.iot.mqtt.common.subscripe.ISubscribeStoreService;
import cn.jasper.iot.mqtt.config.BrokerProperties;
import cn.jasper.iot.mqtt.store.cache.ChannelCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CONNECT连接处理
 */
public class Connect {

	private static final Logger LOGGER = LoggerFactory.getLogger(Connect.class);
	private ISessionStoreService sessionStoreService;

	private ISubscribeStoreService subscribeStoreService;

	private IDupPublishMessageStoreService dupPublishMessageStoreService;

	private IDupPubRelMessageStoreService dupPubRelMessageStoreService;

	private IAuthService authService;
	private ChannelCache channelCache;
	private BrokerProperties brokerProperties;

	public Connect(BrokerProperties brokerProperties, ChannelCache channelCache, ISessionStoreService sessionStoreService, ISubscribeStoreService subscribeStoreService, IDupPublishMessageStoreService dupPublishMessageStoreService, IDupPubRelMessageStoreService dupPubRelMessageStoreService, IAuthService authService) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
		this.authService = authService;
		this.channelCache = channelCache;
		this.brokerProperties = brokerProperties;
	}

	public void processConnect(Channel channel, MqttConnectMessage msg) {
		//解码失败
		if (decodeFailure(channel, msg)){ return;}
		//ClientId为空
		if (nullClientId(channel, msg)) {
			return;
		}
		//检查用户和密码
		if (checkUserAndPassword(channel, msg)) {
			return;
		}//检查Session，是否存储过客户端
		checkSession(msg);
		SessionStore sessionStore = new SessionStore(brokerProperties.getId(), msg.payload().clientIdentifier(), channel.id().asLongText(), msg.variableHeader().isCleanSession(), null);
		//处理遗嘱消息
		processWillMsg(channel, msg,sessionStore);
		//使用客户端的心跳时间
		processIdle(channel, msg);
		//确认连接请求
		connaACK(channel, msg,sessionStore);
		//处理消息重发
		processDUP(channel, msg);
	}

	private void processDUP(Channel channel, MqttConnectMessage msg) {
		// 如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
		if (!msg.variableHeader().isCleanSession()) {
			List<DupPublishMessageStore> dupPublishMessageStoreList = dupPublishMessageStoreService.get(msg.payload().clientIdentifier());
			List<DupPubRelMessageStore> dupPubRelMessageStoreList = dupPubRelMessageStoreService.get(msg.payload().clientIdentifier());
			dupPublishMessageStoreList.forEach(dupPublishMessageStore -> {
				MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dupPublishMessageStore.getMqttQoS()), false, 0),
					new MqttPublishVariableHeader(dupPublishMessageStore.getTopic(), dupPublishMessageStore.getMessageId()), Unpooled.buffer().writeBytes(dupPublishMessageStore.getMessageBytes()));
				channel.writeAndFlush(publishMessage);
			});
			dupPubRelMessageStoreList.forEach(dupPubRelMessageStore -> {
				MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
					MqttMessageIdVariableHeader.from(dupPubRelMessageStore.getMessageId()), null);
				channel.writeAndFlush(pubRelMessage);
			});
		}
	}

	private void connaACK(Channel channel, MqttConnectMessage msg,SessionStore sessionStore) {
		// 至此存储会话信息及返回接受客户端连接
		sessionStoreService.put(msg.payload().clientIdentifier(), sessionStore);
		// 将clientId存储到channel的map中
		channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
		Boolean sessionPresent = sessionStoreService.containsKey(msg.payload().clientIdentifier()) && !msg.variableHeader().isCleanSession();
		MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
		channel.writeAndFlush(okResp);
		LOGGER.debug("CONNECT - clientId: {}, cleanSession: {}", msg.payload().clientIdentifier(), msg.variableHeader().isCleanSession());
	}

	private void processIdle(Channel channel, MqttConnectMessage msg) {
		// 处理连接心跳包
		if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
			if (channel.pipeline().names().contains("idle")) {
				channel.pipeline().remove("idle");
			}
			channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
		}
	}

	private void processWillMsg(Channel channel, MqttConnectMessage msg,SessionStore sessionStore) {
		// 处理遗嘱信息
		if (msg.variableHeader().isWillFlag()) {
			MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
				new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
			sessionStore.setWillMessage(willMessage);
		}
	}

	private void checkSession(MqttConnectMessage msg) {
		// 如果会话中已存储这个新连接的clientId, 就关闭之前该clientId的连接
		if (sessionStoreService.containsKey(msg.payload().clientIdentifier())) {
			SessionStore sessionStore = sessionStoreService.get(msg.payload().clientIdentifier());
			boolean cleanSession = sessionStore.isCleanSession();
			if (cleanSession) {
				//如果清理会话（CleanSession）标志被设置为1，客户端和服务端必须丢弃之前的任何会话并开始一个新的会话
				sessionStoreService.remove(msg.payload().clientIdentifier());
				subscribeStoreService.removeForClient(msg.payload().clientIdentifier());
				dupPublishMessageStoreService.removeByClient(msg.payload().clientIdentifier());
				dupPubRelMessageStoreService.removeByClient(msg.payload().clientIdentifier());
			}
			try {
				//删除之前的Channel
				channelCache.remove(sessionStore.getChannelId());
			} catch (Exception e) {
				//e.printStackTrace();
			}
		} else {
			//如果不存在session，则清除之前的其他缓存
			subscribeStoreService.removeForClient(msg.payload().clientIdentifier());
			dupPublishMessageStoreService.removeByClient(msg.payload().clientIdentifier());
			dupPubRelMessageStoreService.removeByClient(msg.payload().clientIdentifier());
		}
	}

	private boolean checkUserAndPassword(Channel channel, MqttConnectMessage msg) {
		if(!brokerProperties.isUserAuthEnabled()){
			return false;
		}
		// 用户名和密码验证
		String username = msg.payload().userName();
		String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), CharsetUtil.UTF_8);
		if (!authService.checkValid(username, password)) {
			MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
				new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
			channel.writeAndFlush(connAckMessage);
			channel.close();
			return true;
		}
		return false;
	}

	private boolean nullClientId(Channel channel, MqttConnectMessage msg) {
		// clientId为空或null的情况, 这里要求客户端必须提供clientId, 不管cleanSession是否为1, 此处没有参考标准协议实现
		if (StringUtil.isNullOrEmpty(msg.payload().clientIdentifier())) {
			MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
				new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
			channel.writeAndFlush(connAckMessage);
			channel.close();
			return true;
		}
		return false;
	}

	private boolean decodeFailure(Channel channel, MqttConnectMessage msg) {
		// 消息解码器出现异常
		if (msg.decoderResult().isFailure()) {
			Throwable cause = msg.decoderResult().cause();
			if (cause instanceof MqttUnacceptableProtocolVersionException) {
				// 不支持的协议版本
				MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
					new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
				channel.writeAndFlush(connAckMessage);
				channel.close();
				return true;
			} else if (cause instanceof MqttIdentifierRejectedException) {
				// 不合格的clientId
				MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
					new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
				channel.writeAndFlush(connAckMessage);
				channel.close();
				return true;
			}
			channel.close();
			return true;
		}
		return false;
	}

}
