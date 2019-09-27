package com.rengu.operationsmanagementsuitev3;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


//@EnableDubboConfiguration

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class OperationsManagementSuiteV3Application {

    public static void main(String[] args) {
        SpringApplication.run(OperationsManagementSuiteV3Application.class, args);
    }
}
