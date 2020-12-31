package cn.jasper.iot.mqtt.server;

import cn.jasper.iot.mqtt.config.BrokerProperties;
import cn.jasper.iot.mqtt.handler.BrokerHandler;
import cn.jasper.iot.mqtt.protocol.ProtocolProcess;
import cn.jasper.iot.mqtt.handler.TcpTimeoutHandler;
import cn.jasper.iot.mqtt.store.cache.ChannelCache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @description: Netty启动
 * @author: jasper
 * @create: 2020-12-22 17:08
 */
@Component
public class BrokerServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerServer.class);

    @Autowired
    private BrokerProperties brokerProperties;

    @Autowired
    private ProtocolProcess protocolProcess;
    @Autowired
    private ChannelCache channelCache;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private SslContext sslContext;

    private Channel channel;

    private Channel websocketChannel;

    @PostConstruct
    public void start() throws Exception {
        LOGGER.info("Initializing {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        bossGroup = brokerProperties.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        workerGroup = brokerProperties.isUseEpoll() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        //启用SSL
        if(brokerProperties.isSslEnabled()){
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("keystore/mqtt-broker.pfx");
            keyStore.load(inputStream, brokerProperties.getSslPassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, brokerProperties.getSslPassword().toCharArray());
            sslContext = SslContextBuilder.forServer(kmf).build();
        }
        //MQTT启动
        mqttServer();
        //WebSocket启动
        if(brokerProperties.isWebsocketEnabled()){
            websocketServer();
            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} WebSocketPort: {}", "[" + brokerProperties.getId() + "]", brokerProperties.getPort(), brokerProperties.getWebsocketPort());
        }else {

            LOGGER.info("MQTT Broker {} is up and running. Open Port: {} ", "[" + brokerProperties.getId() + "]", brokerProperties.getPort());
        }

    }

    @PreDestroy
    public void stop() {
        LOGGER.info("Shutdown {} MQTT Broker ...", "[" + brokerProperties.getId() + "]");
        bossGroup.shutdownGracefully();
        bossGroup = null;
        workerGroup.shutdownGracefully();
        workerGroup = null;
        channel.closeFuture().syncUninterruptibly();
        channel = null;
        websocketChannel.closeFuture().syncUninterruptibly();
        websocketChannel = null;
        LOGGER.info("MQTT Broker {} shutdown finish.", "[" + brokerProperties.getId() + "]");
    }

    //MQTT服务启动
    private void mqttServer() throws InterruptedException {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(brokerProperties.isUseEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // handler在初始化时就会执行
                .handler(new LoggingHandler(LogLevel.INFO))
                // childHandler会在客户端成功connect后才执行
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        //CONNECT连接检测
                        channelPipeline.addFirst("tcp-timeout",new TcpTimeoutHandler(brokerProperties.getTcpTimeout()));
                        // Netty提供的心跳检测
                        channelPipeline.addFirst("idle", new IdleStateHandler(0, 0, brokerProperties.getKeepAlive()));
                        // Netty提供的SSL处理
                        if(brokerProperties.isSslEnabled()){
                            SSLEngine sslEngine = sslContext.newEngine(socketChannel.alloc());
                            sslEngine.setUseClientMode(false);        // 服务端模式
                            sslEngine.setNeedClientAuth(false);        // 不需要验证客户端
                            channelPipeline.addLast("ssl", new SslHandler(sslEngine));
                        }
                        channelPipeline.addLast("decoder", new MqttDecoder());
                        channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        channelPipeline.addLast("broker", new BrokerHandler(protocolProcess, channelCache));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, brokerProperties.getSoBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, brokerProperties.isSoKeepAlive());
        channel = sb.bind(brokerProperties.getPort()).sync().channel();
    }
    //WebSocket服务启动
    private void websocketServer() {
    }
}
