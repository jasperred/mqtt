#MQTT by Netty
基于Netty+Spring boot+redis+kafka实现MQTT服务的Broker

项目参考：

https://github.com/netty/netty.git

https://gitee.com/recallcode/iot-mqtt-server

https://github.com/Wizzercn/MqttWk.git 

#项目说明

#### 软件架构
1. Netty实现通讯和协议解析
2. Spring Boot实现依赖注入和属性加载
3. Redis实现会话储存
4. Kafka实现消息代理

#### 实现功能
1. 参考MQTT 3.1.1规范实现
2. 消息服务质量QoS（0，1，2）
3. 心跳机制
4. 遗嘱消息、保留消息、消息的分发重试
5. 连接认证
6. SSL连接
7. 主题过滤
8. 消息代理
9. 集群功能