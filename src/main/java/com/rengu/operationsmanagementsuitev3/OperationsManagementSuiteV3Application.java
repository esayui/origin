package com.rengu.operationsmanagementsuitev3;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


//@EnableDubboConfiguration

@EnableCaching
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class OperationsManagementSuiteV3Application {

    public static void main(String[] args) {
        SpringApplication.run(OperationsManagementSuiteV3Application.class, args);
    }
}
