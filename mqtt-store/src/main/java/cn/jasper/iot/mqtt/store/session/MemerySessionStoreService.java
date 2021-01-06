/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.session;


import cn.jasper.iot.mqtt.common.session.ISessionStoreService;
import cn.jasper.iot.mqtt.common.session.SessionStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话存储服务
 * 本地内存存储
 */
@Service
@ConditionalOnProperty(name = {"spring.mqtt.broker.cache"},havingValue = "memory",matchIfMissing = true)
@Deprecated
public class MemerySessionStoreService implements ISessionStoreService {

	private Map<String, SessionStore> sessionCache = new ConcurrentHashMap<String, SessionStore>();

	@Override
	public void put(String clientId, SessionStore sessionStore) {
		sessionCache.put(clientId, sessionStore);
	}

	@Override
	public void expire(String clientId, int expire) {

	}

	@Override
	public SessionStore get(String clientId) {
		return sessionCache.get(clientId);
	}

	@Override
	public boolean containsKey(String clientId) {
		return sessionCache.containsKey(clientId);
	}

	@Override
	public void remove(String clientId) {
		sessionCache.remove(clientId);
	}
}
