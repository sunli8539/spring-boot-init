// package com.smartai.common.config;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.web.client.RestTemplateBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;
//
// import java.time.Duration;
// import java.time.temporal.ChronoUnit;
//
// @Configuration
// public class SpringConfig {
//
//     @Autowired
//     private RestTemplateBuilder templateBuilder;
//
//     @Bean
//     public RestTemplate restTemplate() {
//         templateBuilder.setConnectTimeout(Duration.of(30, ChronoUnit.SECONDS))
//             .setReadTimeout(Duration.of(30, ChronoUnit.SECONDS));
//         return templateBuilder.build();
//     }
// }
//
