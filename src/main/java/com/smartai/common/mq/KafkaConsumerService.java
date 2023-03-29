package com.smartai.common.mq;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "test", groupId = "kafka-test")
    public void listen(String message) {
        System.out.println("接收到消息：" + message);
    }
}
