/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.handler;

import cn.jasper.iot.mqtt.common.session.SessionStore;
import cn.jasper.iot.mqtt.protocol.ProtocolProcess;
import cn.jasper.iot.mqtt.store.cache.ChannelCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.io.IOException;

/**
 * MQTT消息处理
 */
public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {

	private ProtocolProcess protocolProcess;
	private ChannelCache channelCache;

	public BrokerHandler(ProtocolProcess protocolProcess, ChannelCache channelCache) {
		this.protocolProcess = protocolProcess;
		this.channelCache = channelCache;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
		switch (msg.fixedHeader().messageType()) {
			case CONNECT:
				protocolProcess.connect().processConnect(ctx.channel(), (MqttConnectMessage) msg);
				break;
			case CONNACK:
				break;
			case PUBLISH:
				protocolProcess.publish().processPublish(ctx.channel(), (MqttPublishMessage) msg);
				break;
			case PUBACK:
				protocolProcess.pubAck().processPubAck(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
				break;
			case PUBREC:
				protocolProcess.pubRec().processPubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
				break;
			case PUBREL:
				protocolProcess.pubRel().processPubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
				break;
			case PUBCOMP:
				protocolProcess.pubComp().processPubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
				break;
			case SUBSCRIBE:
				protocolProcess.subscribe().processSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
				break;
			case SUBACK:
				break;
			case UNSUBSCRIBE:
				protocolProcess.unSubscribe().processUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
				break;
			case UNSUBACK:
				break;
			case PINGREQ:
				protocolProcess.pingReq().processPingReq(ctx.channel(), msg);
				break;
			case PINGRESP:
				break;
			case DISCONNECT:
				protocolProcess.disConnect().processDisConnect(ctx.channel(), msg);
				break;
			default:
				break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		channelCache.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		channelCache.remove(ctx.channel().id().asLongText());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) {
			// 远程主机强迫关闭了一个现有的连接的异常
			ctx.close();
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
			if (idleStateEvent.state() == IdleState.ALL_IDLE) {
				Channel channel = ctx.channel();
				String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
				System.out.println("receive idle clientId :"+clientId);
				// 发送遗嘱消息
				if (this.protocolProcess.getSessionStoreService().containsKey(clientId)) {
					SessionStore sessionStore = this.protocolProcess.getSessionStoreService().get(clientId);
					if (sessionStore.getWillMessage() != null) {
						this.protocolProcess.publish().processPublish(ctx.channel(), sessionStore.getWillMessage());
					}
				}
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
