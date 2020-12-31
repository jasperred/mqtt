/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.protocol;

import cn.jasper.iot.mqtt.common.auth.IAuthService;
import cn.jasper.iot.mqtt.common.message.IDupPubRelMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IDupPublishMessageStoreService;
import cn.jasper.iot.mqtt.common.message.IMessageIdService;
import cn.jasper.iot.mqtt.common.message.IRetainMessageStoreService;
import cn.jasper.iot.mqtt.common.session.ISessionStoreService;
import cn.jasper.iot.mqtt.common.subscripe.ISubscribeStoreService;
import cn.jasper.iot.mqtt.config.BrokerProperties;
import cn.jasper.iot.mqtt.internal.InternalCommunication;
import cn.jasper.iot.mqtt.store.cache.ChannelCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 协议处理
 */
@Component
public class ProtocolProcess {

	@Autowired
	private ISessionStoreService sessionStoreService;

	@Autowired
	private ISubscribeStoreService subscribeStoreService;

	@Autowired
	private IAuthService authService;

	@Autowired
	private IMessageIdService messageIdService;

	@Autowired
	private IRetainMessageStoreService messageStoreService;

	@Autowired
	private IDupPublishMessageStoreService dupPublishMessageStoreService;

	@Autowired
	private IDupPubRelMessageStoreService dupPubRelMessageStoreService;
	@Autowired
	private ChannelCache channelCache;

	@Autowired
	private InternalCommunication internalCommunication;

	@Autowired
	private BrokerProperties brokerProperties;
	private Connect connect;

	private Subscribe subscribe;

	private UnSubscribe unSubscribe;

	private Publish publish;

	private DisConnect disConnect;

	private PingReq pingReq;

	private PubRel pubRel;

	private PubAck pubAck;

	private PubRec pubRec;

	private PubComp pubComp;

	public Connect connect() {
		if (connect == null) {
			connect = new Connect( brokerProperties, channelCache,  sessionStoreService,  subscribeStoreService,  dupPublishMessageStoreService,  dupPubRelMessageStoreService,  authService);
		}
		return connect;
	}

	public Subscribe subscribe() {
		if (subscribe == null) {
			subscribe = new Subscribe(subscribeStoreService, messageIdService, messageStoreService);
		}
		return subscribe;
	}

	public UnSubscribe unSubscribe() {
		if (unSubscribe == null) {
			unSubscribe = new UnSubscribe(subscribeStoreService);
		}
		return unSubscribe;
	}

	public Publish publish() {
		if (publish == null) {
			publish = new Publish(sessionStoreService, subscribeStoreService, messageIdService, messageStoreService, dupPublishMessageStoreService, internalCommunication, channelCache);
		}
		return publish;
	}

	public DisConnect disConnect() {
		if (disConnect == null) {
			disConnect = new DisConnect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return disConnect;
	}

	public PingReq pingReq() {
		if (pingReq == null) {
			pingReq = new PingReq();
		}
		return pingReq;
	}

	public PubRel pubRel() {
		if (pubRel == null) {
			pubRel = new PubRel(dupPublishMessageStoreService);
		}
		return pubRel;
	}

	public PubAck pubAck() {
		if (pubAck == null) {
			pubAck = new PubAck(messageIdService, dupPublishMessageStoreService);
		}
		return pubAck;
	}

	public PubRec pubRec() {
		if (pubRec == null) {
			pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return pubRec;
	}

	public PubComp pubComp() {
		if (pubComp == null) {
			pubComp = new PubComp(messageIdService, dupPubRelMessageStoreService);
		}
		return pubComp;
	}

	public ISessionStoreService getSessionStoreService() {
		return sessionStoreService;
	}

}
