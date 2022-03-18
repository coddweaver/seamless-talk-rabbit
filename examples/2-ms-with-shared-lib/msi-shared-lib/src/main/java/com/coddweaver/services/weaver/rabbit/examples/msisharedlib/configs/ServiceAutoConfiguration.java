package com.coddweaver.services.weaver.rabbit.examples.msisharedlib.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.coddweaver.services.weaver.rabbit.examples.msisharedlib")
public class ServiceAutoConfiguration {

    public ServiceAutoConfiguration() {
        int a = 5;
    }
}
