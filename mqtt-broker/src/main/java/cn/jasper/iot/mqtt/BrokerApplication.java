package cn.jasper.iot.mqtt;

import cn.jasper.iot.mqtt.config.BrokerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @description: 启动类
 * @author: jasper
 * @create: 2020-12-22 15:46
 */
@SpringBootApplication(scanBasePackages = {"cn.jasper.iot.mqtt"})
public class BrokerApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BrokerApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Bean
    public BrokerProperties brokerProperties() {
        return new BrokerProperties();
    }
}
