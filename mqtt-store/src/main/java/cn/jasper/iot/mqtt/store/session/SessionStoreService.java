/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.store.session;


import cn.jasper.iot.mqtt.common.session.ISessionStoreService;
import cn.jasper.iot.mqtt.common.session.SessionStore;
import cn.jasper.iot.mqtt.common.subscripe.SubscribeStore;
import cn.jasper.iot.mqtt.store.cache.CacheService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话存储服务
 * Redis存储
 */
@Service
public class SessionStoreService implements ISessionStoreService {
	private final static String CACHE_PRE = "mqtt:session:";
	@Autowired
	private CacheService cacheService;

	@Override
	public void put(String clientId, SessionStore sessionStore) {
		cacheService.put(CACHE_PRE + clientId, JSONObject.toJSONString(sessionStore));
	}

	@Override
	public void expire(String clientId, int expire) {
		cacheService.expire(CACHE_PRE + clientId, expire);
	}


	@Override
	public SessionStore get(String clientId) {
		return JSONObject.parseObject(cacheService.get(CACHE_PRE + clientId), SessionStore.class);
	}

	@Override
	public boolean containsKey(String clientId) {
		return cacheService.containsKey(CACHE_PRE + clientId);
	}


	@Override
	@Async
	public void remove(String clientId) {
		cacheService.remove(CACHE_PRE + clientId);
	}

}
