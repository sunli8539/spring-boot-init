package com.smartai.common.sqlprint;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.addInterceptor(new PrintSqlInterceptor());
    }
}
