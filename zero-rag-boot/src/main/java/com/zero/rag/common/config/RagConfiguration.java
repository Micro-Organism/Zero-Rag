package com.zero.rag.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RagConfiguration {

    @Bean
    public ApplicationRunner atStartup() {
        return args -> log.info("Spring Boot DOC Chatbot is running!");
    }

}
