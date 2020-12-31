package cn.jasper.iot.mqtt.store.cache;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.PlatformDependent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @description: 缓存Channel
 * 本地内存缓存
 * @author: jasper
 * @create: 2020-12-25 11:02
 */
@Service
public class ChannelCache {

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private Map<String, ChannelId> channelIdMap = new HashMap<>();
    public boolean add(Channel channel) {
        if(channel==null){
            return false;
        }
        channelIdMap.put(channel.id().asLongText(),channel.id());
        return channelGroup.add(channel);
    }

    public Channel find(String channelId) {
        ChannelId id = channelIdMap.get(channelId);
        if(id==null){
            return null;
        }else {
            return channelGroup.find(id);
        }

    }

    public boolean remove(String channelId){
        ChannelId id = channelIdMap.get(channelId);
        if(id==null){
            return false;
        }else {
            channelIdMap.remove(channelId);
            return channelGroup.remove(id);
        }
    }

}
