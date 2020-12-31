/**
 * Copyright (c) 2018, Mr.Wang (recallcode@aliyun.com) All rights reserved.
 */

package cn.jasper.iot.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 服务配置
 */
@ConfigurationProperties(prefix = "spring.mqtt.broker")
public class BrokerProperties {

	/**
	 * Broker唯一标识
	 */
	private String id;

	/**
	 *是否启用SSL，默认启用
	 */
	private boolean sslEnabled = true;

	/**
	 * 端口号, 默认8883端口
	 */
	private int port = 8883;

	/**
	 * WebSocket 端口号, 默认443端口
	 */
	private int websocketPort = 443;

	/**
	 * 是否启用WebSocket
	 */
	private boolean websocketEnabled = true;
	/**
	 * WebSocket Path值, 默认值 /mqtt
	 */
	private String websocketPath = "/mqtt";

	/**
	 * SSL密钥文件密码
	 */
	private String sslPassword;

	/**
	 * 是否启用用户验证，需要验证用户名和密码
	 * 默认开启
	 */
	private boolean userAuthEnabled = true;
	/**
	 * 心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖
	 */
	private int keepAlive = 60;

	/**
	 * 收到连接后如果Timeout时间内没有确认CONNECT则关闭连接
	 * 设置0表示不开启该检测
	 */
	private int tcpTimeout = 6;
	/**
	 * 是否开启Epoll模式, 默认关闭
	 */
	private boolean useEpoll = false;

	/**
	 * Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
	 */
	private int soBacklog = 511;

	/**
	 * Socket参数, 是否开启心跳保活机制, 默认开启
	 */
	private boolean soKeepAlive = true;

	/**
	 * 集群配置, 是否基于组播发现, 默认开启
	 */
	private boolean enableMulticastGroup = true;

	/**
	 * 集群配置, 基于组播发现
	 */
	private String multicastGroup;

	/**
	 * 集群配置, 当组播模式禁用时, 使用静态IP开启配置集群
	 */
	private String staticIpAddresses;

	public String getId() {
		return id;
	}

	public BrokerProperties setId(String id) {
		this.id = id;
		return this;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public BrokerProperties setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	public int getPort() {
		return port;
	}

	public BrokerProperties setPort(int port) {
		this.port = port;
		return this;
	}

	public boolean isWebsocketEnabled() {
		return websocketEnabled;
	}

	public BrokerProperties setWebsocketEnabled(boolean websocketEnabled) {
		this.websocketEnabled = websocketEnabled;
		return this;
	}

	public boolean isUserAuthEnabled() {
		return userAuthEnabled;
	}

	public BrokerProperties setUserAuthEnabled(boolean userAuthEnabled) {
		this.userAuthEnabled = userAuthEnabled;
		return this;
	}

	public int getWebsocketPort() {
		return websocketPort;
	}

	public BrokerProperties setWebsocketPort(int websocketPort) {
		this.websocketPort = websocketPort;
		return this;
	}

	public String getWebsocketPath() {
		return websocketPath;
	}

	public BrokerProperties setWebsocketPath(String websocketPath) {
		this.websocketPath = websocketPath;
		return this;
	}

	public String getSslPassword() {
		return sslPassword;
	}

	public BrokerProperties setSslPassword(String sslPassword) {
		this.sslPassword = sslPassword;
		return this;
	}

	public int getKeepAlive() {
		return keepAlive;
	}

	public BrokerProperties setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}

	public int getTcpTimeout() {
		return tcpTimeout;
	}

	public BrokerProperties setTcpTimeout(int tcpTimeout) {
		this.tcpTimeout = tcpTimeout;
		return this;
	}

	public boolean isUseEpoll() {
		return useEpoll;
	}

	public BrokerProperties setUseEpoll(boolean useEpoll) {
		this.useEpoll = useEpoll;
		return this;
	}

	public int getSoBacklog() {
		return soBacklog;
	}

	public BrokerProperties setSoBacklog(int soBacklog) {
		this.soBacklog = soBacklog;
		return this;
	}

	public boolean isSoKeepAlive() {
		return soKeepAlive;
	}

	public BrokerProperties setSoKeepAlive(boolean soKeepAlive) {
		this.soKeepAlive = soKeepAlive;
		return this;
	}

	public boolean isEnableMulticastGroup() {
		return enableMulticastGroup;
	}

	public BrokerProperties setEnableMulticastGroup(boolean enableMulticastGroup) {
		this.enableMulticastGroup = enableMulticastGroup;
		return this;
	}

	public String getMulticastGroup() {
		return multicastGroup;
	}

	public BrokerProperties setMulticastGroup(String multicastGroup) {
		this.multicastGroup = multicastGroup;
		return this;
	}

	public String getStaticIpAddresses() {
		return staticIpAddresses;
	}

	public BrokerProperties setStaticIpAddresses(String staticIpAddresses) {
		this.staticIpAddresses = staticIpAddresses;
		return this;
	}
}
