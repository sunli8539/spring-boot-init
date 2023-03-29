package com.smartai.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.cluster.nodes}")
    private String nodes;


    @Autowired
    private RestTemplateBuilder templateBuilder;

    @Bean
    public RestTemplate restTemplate() {
        templateBuilder.setConnectTimeout(Duration.of(30, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(30, ChronoUnit.SECONDS));
        return templateBuilder.build();
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
        @Qualifier("redissonConnectionFactory") RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决redisson无法反序列化LocalDatetime问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new DateSerializedConfig());
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * defaultConfig()
     *
     * @return Config
     */
    @Bean
    public Config defaultConfig() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("redisson/redisson-config.yml");
        return getConfig(inputStream);
    }

    private Config getConfig(InputStream inputStream) throws IOException {
        try {
            return createConfig(inputStream);
        } finally {
            try {
                if (Objects.nonNull(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("inputStream.close error", e);
            }
        }
    }

    /**
     * 创建配置
     *
     * @param inputStream InputStream
     * @return Config
     */
    private Config createConfig(InputStream inputStream) throws IOException {
        Config config = Config.fromYAML(inputStream);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决redisson无法反序列化LocalDatetime问题
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new DateSerializedConfig());
        config.setCodec(new JsonJacksonCodec(mapper));
        // 方便把redis集群配置到nacos或者配置中心
        List<String> adressList = Arrays.stream(nodes.split(","))
            .map(node -> "redis://" + node)
            .collect(Collectors.toList());
        config.useClusterServers().setNodeAddresses(adressList);
        config.useClusterServers().setPassword(password);
        return config;
    }

    /**
     * redissonClient(Config config)
     */
    @Bean
    public RedissonClient redissonClient(Config config) {
        return Redisson.create(config);
    }
}
