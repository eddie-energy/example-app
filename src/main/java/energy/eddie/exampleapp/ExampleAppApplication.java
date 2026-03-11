package energy.eddie.exampleapp;

import energy.eddie.exampleapp.config.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "energy.eddie", defaultConfiguration = FeignClientConfig.class)
@EnableConfigurationProperties({ExampleAppConfig.class, ExampleAppEddieConfig.class, ExampleAppKafkaConfig.class, ExampleAppMqttConfig.class})
public class ExampleAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleAppApplication.class, args);
    }
}
