package cn.jasper.iot.mqtt.store.utils;

import cn.jasper.iot.mqtt.common.session.SessionStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by cs on 2018
 */
public class StoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreUtil.class);

    public static Map transPublishToMapBeta(SessionStore store) {
        try {
            Map sessionStore = new LinkedHashMap<>();
            sessionStore.put("clientId", store.getClientId());
            sessionStore.put("channelId", store.getChannelId());
            sessionStore.put("cleanSession", store.isCleanSession());
            sessionStore.put("brokerId", store.getBrokerId());
            sessionStore.put("expire", store.getExpire());
            MqttPublishMessage msg = store.getWillMessage();
            if (null != msg) {
                Map<String,Object> mm = new LinkedHashMap<>();
                mm.put("payload", Base64.getEncoder().encodeToString(msg.payload().array()));
                mm.put("messageType", msg.fixedHeader().messageType().value());
                mm.put("isDup", msg.fixedHeader().isDup());
                mm.put("qosLevel", msg.fixedHeader().qosLevel().value());
                mm.put("isRetain", msg.fixedHeader().isRetain());
                mm.put("remainingLength", msg.fixedHeader().remainingLength());
                mm.put("topicName", msg.variableHeader().topicName());
                mm.put("packetId", msg.variableHeader().packetId());
                mm.put("hasWillMessage", true);
                sessionStore.put("mqttPublishMessage",mm);
            }

            return sessionStore;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }


    public static SessionStore mapTransToPublishMsgBeta(Map store) {
        SessionStore sessionStore = new SessionStore();
        Map<String,Object> mm = (Map<String, Object>) store.get("mqttPublishMessage");
        if (mm!=null) {
            byte[] payloads = Base64.getDecoder().decode(getString(mm.get("payload")));
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(payloads.length);
            buf.writeBytes(payloads);
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                    MqttMessageType.valueOf(getInt(mm.get("messageType"))),
                    getBoolean(mm.get("isDup")),
                    MqttQoS.valueOf(getInt(mm.get("qosLevel"))),
                    getBoolean(mm.get("isRetain")),
                    getInt(mm.get("remainingLength")));

            MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(getString(store.get("topicName")),
                    getInt(mm.get("packetId")));

            MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, buf);
            sessionStore.setWillMessage(mqttPublishMessage);
        }
        sessionStore.setChannelId(getString(store.get("channelId")));
        sessionStore.setClientId(getString(store.get("clientId")));
        sessionStore.setCleanSession(getBoolean(store.get("cleanSession")));
        sessionStore.setBrokerId(getString(store.get("brokerId")));
        sessionStore.setExpire(getInt(store.get("expire")));
        return sessionStore;
    }

    private static String getString(Object obj) {
        return obj==null?"":obj.toString();
    }
    private static int getInt(Object obj) {
        return obj==null?0:Integer.parseInt(obj.toString());
    }
    private static boolean getBoolean(Object obj) {
        return obj==null?false:Boolean.parseBoolean(obj.toString());
    }
}
